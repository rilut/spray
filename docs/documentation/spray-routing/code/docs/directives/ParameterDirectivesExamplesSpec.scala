package docs.directives

import spray.http.StatusCodes

class ParameterDirectivesExamplesSpec extends DirectivesSpec {
  "example-1" in {
    val route =
      parameter('color) { color =>
        complete(s"The color is '$color'")
      }

    Get("/?color=blue") ~> route ~> check {
      responseAs[String] === "The color is 'blue'"
    }

    Get("/") ~> sealRoute(route) ~> check {
      status === StatusCodes.NotFound
      responseAs[String] === "Request is missing required query parameter 'color'"
    }
  }
  "required-1" in {
    val route =
      parameters('color, 'backgroundColor) { (color, backgroundColor) =>
        complete(s"The color is '$color' and the background is '$backgroundColor'")
      }

    Get("/?color=blue&backgroundColor=red") ~> route ~> check {
      responseAs[String] === "The color is 'blue' and the background is 'red'"
    }
    Get("/?color=blue") ~> sealRoute(route) ~> check {
      status === StatusCodes.NotFound
      responseAs[String] === "Request is missing required query parameter 'backgroundColor'"
    }
  }
  "optional" in {
    val route =
      parameters('color, 'backgroundColor.?) { (color, backgroundColor) =>
        val backgroundStr = backgroundColor.getOrElse("<undefined>")
        complete(s"The color is '$color' and the background is '$backgroundStr'")
      }

    Get("/?color=blue&backgroundColor=red") ~> route ~> check {
      responseAs[String] === "The color is 'blue' and the background is 'red'"
    }
    Get("/?color=blue") ~> route ~> check {
      responseAs[String] === "The color is 'blue' and the background is '<undefined>'"
    }
  }
  "optional-with-default" in {
    val route =
      parameters('color, 'backgroundColor ? "white") { (color, backgroundColor) =>
        complete(s"The color is '$color' and the background is '$backgroundColor'")
      }

    Get("/?color=blue&backgroundColor=red") ~> route ~> check {
      responseAs[String] === "The color is 'blue' and the background is 'red'"
    }
    Get("/?color=blue") ~> route ~> check {
      responseAs[String] === "The color is 'blue' and the background is 'white'"
    }
  }
  "required-value" in {
    val route =
      parameters('color, 'action ! "true") { (color) =>
        complete(s"The color is '$color'.")
      }

    Get("/?color=blue&action=true") ~> route ~> check {
      responseAs[String] === "The color is 'blue'."
    }

    Get("/?color=blue&action=false") ~> sealRoute(route) ~> check {
      status === StatusCodes.NotFound
      responseAs[String] === "The requested resource could not be found."
    }
  }
  "mapped-value" in {
    val route =
      parameters('color, 'count.as[Int]) { (color, count) =>
        complete(s"The color is '$color' and you have $count of it.")
      }

    Get("/?color=blue&count=42") ~> route ~> check {
      responseAs[String] === "The color is 'blue' and you have 42 of it."
    }

    Get("/?color=blue&count=blub") ~> sealRoute(route) ~> check {
      status === StatusCodes.BadRequest
      responseAs[String] === "The query parameter 'count' was malformed:\n'blub' is not a valid 32-bit integer value"
    }
  }
  "parameterMap" in {
    val route =
      parameterMap { params =>
        def paramString(param: (String, String)): String = s"""${param._1} = '${param._2}'"""
        complete(s"The parameters are ${params.map(paramString).mkString(", ")}")
      }

    Get("/?color=blue&count=42") ~> route ~> check {
      responseAs[String] === "The parameters are color = 'blue', count = '42'"
    }
    Get("/?x=1&x=2") ~> route ~> check {
      responseAs[String] === "The parameters are x = '2'"
    }
  }
  "parameterMultiMap" in {
    val route =
      parameterMultiMap { params =>
        complete(s"There are parameters ${params.map(x => x._1+" -> "+x._2.size).mkString(", ")}")
      }

    Get("/?color=blue&count=42") ~> route ~> check {
      responseAs[String] === "There are parameters color -> 1, count -> 1"
    }
    Get("/?x=23&x=42") ~> route ~> check {
      responseAs[String] === "There are parameters x -> 2"
    }
  }
  "parameterSeq" in {
    val route =
      parameterSeq { params =>
        def paramString(param: (String, String)): String = s"""${param._1} = '${param._2}'"""
        complete(s"The parameters are ${params.map(paramString).mkString(", ")}")
      }

    Get("/?color=blue&count=42") ~> route ~> check {
      responseAs[String] === "The parameters are color = 'blue', count = '42'"
    }
    Get("/?x=1&x=2") ~> route ~> check {
      responseAs[String] === "The parameters are x = '1', x = '2'"
    }
  }
}
