# SBT Swagger Route Gen

An SBT plugin that takes a `swagger.yml` file in the root of your project, parses it for its routes, and then generates a http4s `HttpRoutes[IO]` object that conforms to those routes.
Currently there is only a "raw" version which expects you to parse the body of the `Request[IO]` and produce a `IO[Response[IO]]` yourself.

## Usage:
A trait `SwaggerRoutes` is generated where which method is a route/method combination. You need to implement each method here like:
```
  case class MyRoutesObject(myService: MyService) extends SwaggerRoutes {
    override def GET_/(_request: Request[IO]): IO[Response[IO]] = Ok()

    override def `POST_/thing/{id}/do`(request: Request[IO], virtualDeviceId: String): IO[Response[IO]] =
      for {
        responseBody <- myService.doThing(id)
        response <- Ok(responseBody)
      } yield response
  }
```

Then you can use `SwaggerRouteHelper` to turn this into a `HttpRoutes[IO]` and run it (or add middleware etc)like you would any other, for example:
```
class MyApi extends IOApp with Http4sDsl[IO] {
  override def run(args: List[String]): IO[ExitCode] = for {
    myService <- Stream(new MyService())
    routes = SwaggerRouteHelper.makeRoutes(MyRoutesObject(myService))
    _ <- BlazeBuilder[IO]
      .bindHttp(8080)
      .mountService(routes, "/")
      .serve
      .compile
      .drain
  } yield ExitCode.Error
}

```

## Features
- Path parameters

## Missing features
- Query parameters
- Request body handling
- Response body handling
- Non-IO effect types
- support http4s versions before 0.19