//> using scala 3.3.1
//> using file OptionFlatMapView.scala
//> using file OptionMapView.scala
//> using file Helpers.scala

class Foo:
  val value = Some("abc")
  def methodNoParams = Some(true)
  def methodEmptyParens() = Some(0)
  def methodSingleArg(i: Int) = Some(i + 1)
  def methodTwoArgs(i: Int, j: Int) = Some(i + j)

val opt: Option[Foo] = Option(new Foo)

// this whole thing was devised by Michał Pałka ( @prolativ )
@main def run() = 
  println(opt.?.value) // Some(Some(abc))
  println(opt.??.value) // Some(abc)

  println(opt.?.methodNoParams) // Some(Some(true))
  println(opt.??.methodNoParams) // Some(true)

  println(opt.?.methodEmptyParens()) // Some(Some(0))
  println(opt.??.methodEmptyParens()) // Some(0)

  println(opt.?.methodSingleArg(0)) // Some(Some(1))
  println(opt.??.methodSingleArg(0)) // Some(1)

  println(opt.?.methodTwoArgs(1, 2)) // Some(Some(3))
  println(opt.??.methodTwoArgs(1, 2)) // Some(3)
