//> using scala 3.3.1

object contextproxies:
  case class OptionsForA(a: Int)
  case class OptionsForB(b: String)

  sealed trait Variant:
    type Constructor
    val constructor: Constructor

  object Variant:
    trait A extends Variant:
      type Constructor = OptionsForA.type
      val constructor = OptionsForA
    object A extends A

    trait B extends Variant:
      type Constructor = OptionsForB.type
      val constructor = OptionsForB
    object B

  def opts(using v: Variant): v.Constructor = v.constructor

  def apiForA(arg: String, options: OptionsForA): Unit = ???
  def smartApiForA(arg: String, options: Variant.A ?=> OptionsForA): Unit = ???
  def apiForB(arg: Int, options: OptionsForB): Unit = ???
  def smartApiForB(arg: Int, options: Variant.B ?=> OptionsForB): Unit = ???

@main def testContextProxies(): Unit =
  import contextproxies.*
  apiForA("ok", OptionsForA(42))
  apiForB(23, OptionsForB("uhhhh"))

  smartApiForA("nice", opts(42))
  smartApiForB(5, opts("hello"))