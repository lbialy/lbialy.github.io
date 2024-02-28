//> using scala 3.3.1

object inputs:
  opaque type Optional[+A] >: A | Option[A] = A | Option[A]

  given {} with
    extension [A](x: Optional[A])
      def toOption: Option[A] = x match
        case x: Option[A] @unchecked => x
        case x: A @unchecked         => Some(x)

import inputs.*

def myApi(x: Optional[Int] = None): Unit =
  println(x.toOption)

@main def testInputs(): Unit =
  myApi(42)
  myApi(Some(42))
  myApi()
  myApi(None)
