import scala.quoted.*

class OptionFlatMapView[+A](opt: Option[A]) extends Selectable:
  transparent inline def selectDynamic(inline name: String) = ${ optionFlatMapViewSelectDynamicImpl('{opt}, '{name}) }
  transparent inline def applyDynamic(inline name: String)(inline args: Any*) = ${ optionFlatMapViewApplyDynamicImpl('{opt}, '{name}, '{args}) }

def optionFlatMapViewSelectDynamicImpl[A : Type](opt: Expr[Option[A]], name: Expr[String])(using quotes: Quotes): Expr[Any] =
  import quotes.reflect.*
  def selectName(inner: Expr[A]) =
    Select.unique(inner.asTerm, name.valueOrAbort).asExprOf[Option[Any]]
  '{ ${opt}.flatMap(inner => ${selectName('{inner})}) }

def optionFlatMapViewApplyDynamicImpl[A : Type](opt: Expr[Option[A]], name: Expr[String], args: Expr[Seq[Any]])(using quotes: Quotes): Expr[Any] =
  import quotes.reflect.*
  def applyArgsToName(inner: Expr[A], argsExpr: Expr[Seq[Any]]) =
    val argExprs = argsExpr match
      case Varargs(args) =>
        args.map(_.asTerm).toList
    Select.unique(inner.asTerm, name.valueOrAbort).appliedToArgs(argExprs).asExprOf[Option[Any]]
  '{ ${opt}.flatMap(inner => ${applyArgsToName('{inner}, args)}) }

trait OptionFlatMapViewProvider[A]:
  type View <: OptionFlatMapView[A]
  def view(opt: Option[A]): View

object OptionFlatMapViewProvider:
  transparent inline given optionFlatMapViewProvider[A]: OptionFlatMapViewProvider[A] = ${ optionFlatMapViewProviderImpl[A] }

def optionFlatMapViewProviderImpl[A : Type](using quotes: Quotes): Expr[OptionFlatMapViewProvider[A]] =
  import quotes.reflect.*

  val refinements = optionViewRefinements { tp => 
    tp.asType match
      case '[Option[t]] => Some(TypeRepr.of[Option[t]])
      case _ => None
  }
  val refinedType = refineType(TypeRepr.of[OptionFlatMapView[A]], refinements).asType
  refinedType match
    case '[t] => '{ new OptionFlatMapViewProvider[A] {
      override type View = t 
      override def view(opt: Option[A]): View = new OptionFlatMapView(opt).asInstanceOf[t]
    }.asInstanceOf[OptionFlatMapViewProvider[A] { type View = t }] }

extension [A](opt: Option[A])(using p: OptionFlatMapViewProvider[A])
  def ?? : p.View = p.view(opt)