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
    (libraryDependencies in genRoutes) += "org.scalameta" %% "scalameta" % "4.0.0",
    genRoutes := {
      val outputFile = (sourceManaged in Compile).value / "sbt-swagger-gen-routes-generated.scala"
      val sourceFile = "/Users/davidmcgillicuddy/code/etp-diagnostic-api/swagger.yml"
      IO.write(outputFile, genSyntax(sourceFile).syntax, scala.io.Codec.UTF8.charSet)
      Seq(outputFile)
    },
    (sourceGenerators in Compile) += genRoutes.taskValue
  )

  private def genSyntax(fileName: String) = {
    val trySwagger = Try(Option(new SwaggerParser().read(fileName)))
    val swagger: Swagger = trySwagger.get.get // FIXME
    val routeMethods: List[Decl.Def] = mapAsScalaMap(swagger.getPaths).toList.flatMap { case (string, path) =>
      mapAsScalaMap(path.getOperationMap).toList.map { case (method, _) =>
        val name = Term.Name(s"${method.toString}_$string")
        val quote = q"def $name(): Unit"
        println(quote)
        quote
      }
    }
    q"""
       package SbtSwaggerRouteGen {
         trait SwaggerRoutes {
             ..$routeMethods
         }
       }
     """
  }
}
