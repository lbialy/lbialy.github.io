// using scala 3.3.1
// using file macro-based-interpolators.scala

import interpolators.*
import io.circe.*

@main def testJsonInterpolation(): Unit =
  val a = 23
  val b = JsonObject("c" -> Json.fromString("hello")).toJson

  val json: Json = json"""{"a": 23}"""
  println(json) // prints { "a" : 23 }

  val json2: Json = json"""{"a": $a}"""
  println(json2) // prints { "a" : 23 }

  val json3: Json = json"""{"a": $a, "b": $b}"""
  println(json3) // prints { "a" : 23, "b" : { "c" : "hello" } }

  // val json4: Json = json"""{"a": $a, "b": $b, }"""
  // Failed to parse JSON (default values inserted at compile time):
  // expected " got '}' (line 1, column 21)
