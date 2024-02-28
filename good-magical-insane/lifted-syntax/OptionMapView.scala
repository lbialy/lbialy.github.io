import scala.quoted.*

class OptionMapView[+A](opt: Option[A]) extends Selectable:
  transparent inline def selectDynamic(inline name: String) = ${ optionMapViewSelectDynamicImpl('{opt}, '{name}) }
  transparent inline def applyDynamic(inline name: String)(inline args: Any*) = ${ optionMapViewApplyDynamicImpl('{opt}, '{name}, '{args}) }

def optionMapViewSelectDynamicImpl[A : Type](opt: Expr[Option[A]], name: Expr[String])(using quotes: Quotes): Expr[Any] =
  import quotes.reflect.*
  def selectName(inner: Expr[A]) =
    Select.unique(inner.asTerm, name.valueOrAbort).asExprOf[Any]
  '{ ${opt}.map(inner => ${selectName('{inner})}) }

def optionMapViewApplyDynamicImpl[A : Type](opt: Expr[Option[A]], name: Expr[String], args: Expr[Seq[Any]])(using quotes: Quotes): Expr[Any] =
  import quotes.reflect.*
  def applyArgsToName(inner: Expr[A], argsExpr: Expr[Seq[Any]]) =
    val argExprs = argsExpr match
      case Varargs(args) =>
        args.map(_.asTerm).toList
    Select.unique(inner.asTerm, name.valueOrAbort).appliedToArgs(argExprs).asExprOf[Any]
  '{ ${opt}.map(inner => ${applyArgsToName('{inner}, args)}) }

trait OptionMapViewProvider[A]:
  type View <: OptionMapView[A]
  def view(opt: Option[A]): View

object OptionMapViewProvider:
  transparent inline given optionMapViewProvider[A]: OptionMapViewProvider[A] = ${ optionMapViewProviderImpl[A] }

def optionMapViewProviderImpl[A : Type](using quotes: Quotes): Expr[OptionMapViewProvider[A]] =
  import quotes.reflect.*

  val refinements = optionViewRefinements { tp => 
    tp.asType match
      case '[t] => Some(TypeRepr.of[Option[t]])
  }

  val refinedType = refineType(TypeRepr.of[OptionMapView[A]], refinements).asType
  refinedType match
    case '[t] => 
      '{ new OptionMapViewProvider[A] {
        override type View = t 
        override def view(opt: Option[A]): View = new OptionMapView(opt).asInstanceOf[t]
      }.asInstanceOf[OptionMapViewProvider[A] { type View = t }] }

extension [A](opt: Option[A])(using p: OptionMapViewProvider[A])
  def ? : p.View = p.view(opt)