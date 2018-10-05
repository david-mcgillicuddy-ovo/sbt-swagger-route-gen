/*
 * Copyright 2018 OVO Energy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ovoenergy

import scala.collection.mutable
import scala.meta._
import scala.util.matching.Regex

case class SwaggerPath(path: String, method: String, pathPattern: Pat, freeVariables: List[String]) {
  lazy val name: Term.Name = Term.Name(s"${method.toString}_$path")

  lazy val makeDef: Decl.Def = {
    val pathVariables: List[Term.Param] = param"request: Request[IO]" +: freeVariables.map(varName => param"${Term.Name(varName)}: String")
    q"def $name( ..$pathVariables ): IO[Response[IO]]"
  }

  lazy val makeCase: Case = {
    val methodPattern = Term.Name(method.toString)
    val args: List[Term.Name] = ("req" +: freeVariables).map(Term.Name.apply)
    Case(p"req@($methodPattern -> $pathPattern)", None, q"routeImpl.$name( ..$args )")
  }
}

object SwaggerPath {

  def apply(path: String, method: String): SwaggerPath = {
    val (pathPattern, freeVars) = parsePathToHttp4sPath(path)
    new SwaggerPath(path, method, pathPattern, freeVars)
  }

  private val pathVariableRegex: Regex = "^\\{(.*)\\}$".r
  private def parsePathToHttp4sPath(path: String): (Pat, List[String]) = {
    val sections = path.split("/")
    val freeVariables = mutable.MutableList.empty[String]

    val pathPattern = sections.filter(_.nonEmpty).foldLeft[Pat](Term.Name("Root")){(pattern, section) =>
      section match {
        case pathVariableRegex(nonLit) =>
          freeVariables += nonLit
          Pat.ExtractInfix(pattern, Term.Name("/"), List(Pat.Var(Term.Name(nonLit))))
        case _ => Pat.ExtractInfix(pattern, Term.Name("/"), List(Lit.String(section)))
      }
    }
    (pathPattern, freeVariables.toList)
  }
}