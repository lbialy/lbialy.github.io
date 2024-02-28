import scala.quoted.*

extension (using quotes: Quotes)(repr: quotes.reflect.TypeRepr)
  def flatMapResultType(f: quotes.reflect.TypeRepr => Option[quotes.reflect.TypeRepr]): Option[quotes.reflect.TypeRepr] = 
    import quotes.reflect.*
    repr match
      case MethodType(paramNames, paramTypes, resultType) =>
        resultType.flatMapResultType(f).map(tp => MethodType(paramNames)(_ => paramTypes, _ => tp))
      // case PolyType => // should not appear here
      case tp =>
        f(tp)

def refineType(using quotes: Quotes)(base: quotes.reflect.TypeRepr, refinements: Seq[(String, quotes.reflect.TypeRepr)]): quotes.reflect.TypeRepr =
  import quotes.reflect.*
  refinements match
    case Nil => base
    case (label, info) :: tail =>
      val newBase = Refinement(base, label, info)
      refineType(newBase, tail)

def optionViewRefinements[A : Type](using quotes: Quotes)(tryTransformType: quotes.reflect.TypeRepr => Option[quotes.reflect.TypeRepr]): Seq[(String, quotes.reflect.TypeRepr)] =
  import quotes.reflect.*

  def isPolyType(repr: TypeRepr) = repr.widen match
    case _: PolyType => true
    case _ => false
  
  val typeSym = TypeRepr.of[A].typeSymbol

  val fieldRefinements: Seq[(String, quotes.reflect.TypeRepr)] = for
    member <- typeSym.fieldMembers
    tpe <- tryTransformType(member.termRef.widen)
  yield (member.name, tpe)

  val methodRefinements = for
    member <- typeSym.methodMembers
    tpe0 = member.termRef.widen
    if !isPolyType(tpe0)
    tpe <- tpe0.flatMapResultType(tryTransformType)
    asMethodTpe = tpe match
      case x: MethodOrPoly => x
      case x => ByNameType(x)
  yield (member.name, asMethodTpe)

  fieldRefinements ++ methodRefinements
end optionViewRefinements