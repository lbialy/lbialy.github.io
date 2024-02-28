//> using scala 3.3.1

// extremely cursed code, don't even think about doing things like that
// this is just a prototype of syntax, code has to type check only

trait ArgsBuilder

class Unreachable private () // There will never be an instance of this type

object cursednewsdk:
  object Service:
    type Args = ServiceArgsBuilder

  class ServiceArgsBuilder extends ArgsBuilder:
    def spec(using Unreachable) = ??? // stub to make the setter work
    private var spec_ : ServiceSpecArgsBuilder = null
    def spec_=(arg: ServiceSpecArgsBuilder) =
      spec_ = arg

  object ServiceSpec:
    type Args = ServiceSpecArgsBuilder

  class ServiceSpecArgsBuilder extends ArgsBuilder:
    def selector(using Unreachable): Nothing =
      ??? // stub to make the setter work
    private var selector_ : List[String] = null
    def selector_=(arg: List[String]): Unit =
      selector_ = arg

    def ports(using Unreachable): Nothing = ??? // stub to make the setter work
    private var ports_ : List[PortArgsBuilder] = null
    def ports_=(arg: List[PortArgsBuilder]): Unit =
      ports_ = arg

  object Port:
    type Args = PortArgsBuilder

  class PortArgsBuilder extends ArgsBuilder:
    def name(using Unreachable) = ??? // stub to make the setter work
    private var name_ : String = null
    def name_=(arg: String): Unit =
      name_ = arg

    def port(using Unreachable) = ??? // stub to make the setter work
    private var port_ : Int = 0
    def port_=(arg: Int): Unit =
      port_ = arg

  def service(name: String, args: ServiceArgsBuilder) = ???

import cursednewsdk.*

@main def testCursedFeverBracesSyntax(): Unit =
  val labels = List("label1", "label2")

  val port2 = new Port.Args:
    name = "port2"
    port = 456

  val nginxService = service(
    "nginx",
    args = new:
      spec = new:
        selector = labels
        ports = List(
          new {
            name = "port1"
            port = 123
          },
          port2
        )
  )
