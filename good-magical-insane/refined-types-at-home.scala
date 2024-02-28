//> using scala 3.3.1

object types:
  import scala.compiletime.*
  import scala.compiletime.ops.string.*

  opaque type ConstrainedStr <: String = String

  object ConstrainedStr:
    inline def from(s: String): ConstrainedStr =
      requireConst(s)
      inline if !constValue[Matches[s.type, "some-prefix:.+"]] then error("this string doesn't have the necessary prefix of `some-prefix:`")
      else s
  end ConstrainedStr

  inline implicit def str2ConstrainedStr(inline s: String): ConstrainedStr =
    ConstrainedStr.from(s)

import types.*

@main def testConstrainedStr(): Unit =
  val ok: ConstrainedStr = "some-prefix:hello"
  // val fail: ConstrainedStr = "hello"
  
  // if you are brave enough you can see how deep the rabbit hole goes:
  // https://github.com/VirtusLab/besom/blob/main/core/src/main/scala/besom/util/NonEmptyString.scala#L63-L167