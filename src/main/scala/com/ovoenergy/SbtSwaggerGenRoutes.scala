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

import io.swagger.parser.SwaggerParser
import io.swagger.models.Swagger

import scala.collection.JavaConverters._
import sbt.Keys._
import sbt._

import scala.meta._
import scala.util.Try

object SbtSwaggerGenRoutes extends AutoPlugin {
  object autoImport {
    lazy val genRoutes = taskKey[Seq[File]]("Generate a routes object from a swagger file")
  }
  import autoImport._


  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    genRoutes := {
      val outputFile = (sourceManaged in Compile).value / "sbt-swagger-gen-routes-generated.scala"
      val sourceFile = (baseDirectory in Compile).value / "swagger.yml"
      IO.write(outputFile, genSyntax(sourceFile).syntax, scala.io.Codec.UTF8.charSet)
      Seq(outputFile)
    },
    (sourceGenerators in Compile) += genRoutes.taskValue
  )

  private def genSyntax(swaggerFile: File): scala.meta.Pkg = {
    val trySwagger = Try(Option(new SwaggerParser().read(swaggerFile.getAbsolutePath)))
    val swagger: Swagger = trySwagger.get.get // FIXME

    val paths = mapAsScalaMap(swagger.getPaths).toList.flatMap { case (pathPrefix, path) =>
      mapAsScalaMap(path.getOperationMap).toList.map { case (method, _) =>
        SwaggerPath(pathPrefix, method.toString)
      }
    }

    q"""
       package SbtSwaggerRouteGen {
         import cats.effect._
         import org.http4s._
         import org.http4s.dsl.Http4sDsl

         trait SwaggerRoutes {
             ..${paths.map(_.makeDef)}
         }
         object SwaggerRouteHelper extends Http4sDsl[IO] {
           def makeRoutes(routeImpl: SwaggerRoutes): HttpRoutes[IO] = HttpRoutes.of[IO]{
             ..case ${paths.map(_.makeCase)}
           }
         }
       }
     """
  }
}
