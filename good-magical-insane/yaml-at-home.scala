//> using scala 3.3.1

// extremely cursed code, don't even think about doing things like that
// this is just a prototype of syntax, code has to type check only

trait Builder
def *(using b: Builder): b.type = b

object cursedsdkasterisksyntax:

  case class Foo private (
      name: String,
      arg1: Option[Int],
      arg2: Option[String],
      arg3: Seq[Bar.BarArgsBuilder]
  )

  object Foo:
    def make(name: String)(args: FooArgsBuilder ?=> FooArgsBuilder) =
      val a = args(using FooArgsBuilder())
      Foo(name, a._arg1, a._arg2, a._arg3)

    case class FooArgsBuilder private[Foo] (
        private[Foo] val _arg1: Option[Int],
        private[Foo] val _arg2: Option[String],
        private[Foo] val _arg3: Seq[Bar.BarArgsBuilder]
    ) extends Builder:
      def arg1(arg: Int) = copy(_arg1 = Some(arg))
      def arg2(arg: String) = copy(_arg2 = Some(arg))
      def arg3(args: Bar.BarArgsBuilder ?=> Bar.BarArgsBuilder*) =
        copy(_arg3 = args.map(_(using Bar.BarArgsBuilder())))

    object FooArgsBuilder:
      def apply(): FooArgsBuilder =
        FooArgsBuilder(_arg1 = None, _arg2 = None, _arg3 = Seq.empty)

  object Bar:
    case class BarArgsBuilder private[Bar] (
        private val _arg1: Option[String],
        private val _arg2: Option[Boolean]
    ) extends Builder:
      def arg1(arg: String) = copy(_arg1 = Some(arg))
      def arg2(arg: Boolean) = copy(_arg2 = Some(arg))

    object BarArgsBuilder:
      def apply(): BarArgsBuilder = BarArgsBuilder(_arg1 = None, _arg2 = None)

import cursedsdkasterisksyntax.*

// yamlish is valid scala code, you see
@main def testCursedAsteriskSyntax(): Unit =
  val foo = Foo.make("foo1")(
    * arg1 1
      arg2 "qwe"
      arg3 (
        * arg1 "xxx"
          arg2 true,
        * arg1 "xxx"
          arg2 false
      )
  )

  println(foo)
