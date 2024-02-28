//> using dep io.circe::circe-parser:0.14.6

object interpolators:
  import scala.quoted.*
  import io.circe.*, io.circe.parser.*

  val NL = System.lineSeparator()

  def interleave[T](xs: List[T], ys: List[T]): List[T] = (xs, ys) match
    case (Nil, _)           => ys
    case (_, Nil)           => xs
    case (x :: xs, y :: ys) => x :: y :: interleave(xs, ys)

  extension (sc: StringContext) inline def json(inline args: Any*): Json = ${ jsonImpl('sc, 'args) }

  private def jsonImpl(sc: Expr[StringContext], args: Expr[Seq[Any]])(using Quotes): Expr[Json] =
    import quotes.reflect.*

    // this function traverses the tree of the given expression and tries to extract the constant string context tree from it in Right
    // if it fails, it returns a Left with the final tree that couldn't be extracted from
    def resolveStringContext(
        tree: Term,
        i: Int = 0
    ): Either[Term, Seq[Expr[String]]] =
      tree match
        // resolve reference if possible
        case t
            if t.tpe.termSymbol != Symbol.noSymbol && t.tpe <:< TypeRepr
              .of[StringContext] =>
          t.tpe.termSymbol.tree match
            case ValDef(_, _, Some(rhs)) => resolveStringContext(rhs, i + 1)
            case _                       => Left(t)

        // maybe resolved reference?
        case other =>
          tree.asExpr match
            case '{ scala.StringContext.apply(${ Varargs(parts) }: _*) } =>
              Right(parts)
            case _ =>
              Left(other)

    resolveStringContext(sc.asTerm) match
      case Left(badTerm) =>
        report.errorAndAbort(
          s"$sc -> $badTerm is not a string context :O"
        ) // this should never happen

      case Right(parts) =>
        args match
          case Varargs(argExprs) =>
            if argExprs.isEmpty then
              parts.map(_.valueOrAbort).mkString match
                case "" => '{ JsonObject().toJson }
                case str =>
                  parse(str) match
                    case Left(exception) =>
                      report.errorAndAbort(
                        s"Failed to parse JSON:$NL${exception.getMessage}"
                      )
                    case Right(_) =>
                      '{
                        parse(${ Expr(str) }).fold(throw _, identity)
                      }
            else
              val defaults = argExprs.map {
                case '{ $part: String }  => ""
                case '{ $part: Int }     => 0
                case '{ $part: Long }    => 0L
                case '{ $part: Float }   => 0f
                case '{ $part: Double }  => 0d
                case '{ $part: Boolean } => true
                case '{ $part: Json }    => Json.Null
                case '{ $other: t } =>
                  report.errorAndAbort(
                    s"`Value ${other.show}: ${Type.show[t]}` is not a valid JSON interpolation type.$NL$NL" +
                      s"Types Available for interpolation are: " +
                      s"String, Int, Long, Float, Double, Boolean, JsValue and Outputs of those types.$NL" +
                      s"If you want to interpolate a custom data type - derive or implement a JsonFormat for it and convert it to JsValue.$NL$NL"
                  )
              }

              val str = interleave(
                parts.map(_.valueOrAbort).toList,
                defaults.map(_.toString()).toList
              ).reduce(_ + _)

              parse(str) match
                case Left(exception) =>
                  report.errorAndAbort(
                    s"Failed to parse JSON (default values inserted at compile time):$NL  ${exception.getMessage}"
                  )
                case Right(value) =>
                  val liftedSeqOfExpr: Seq[Expr[?]] = argExprs.map {
                    case '{ $part: String }  => ??? // uh, no time to fix, we need to embed the string in ""
                    case '{ $part: Int }     => part
                    case '{ $part: Long }    => part
                    case '{ $part: Float }   => part
                    case '{ $part: Double }  => part
                    case '{ $part: Boolean } => part
                    case '{ $part: Json }    => part
                    case '{ $other: t } =>
                      report.errorAndAbort(
                        s"`Value ${other.show}: ${Type.show[t]}` is not a valid JSON interpolation type.$NL$NL" +
                          s"Types Available for interpolation are: " +
                          s"String, Int, Long, Float, Double, Boolean, JsValue and Outputs of those types.$NL" +
                          s"If you want to interpolate a custom data type - derive or implement a JsonFormat for it and convert it to JsValue.$NL$NL"
                      )
                  }

                  val liftedExprOfSeq = Expr.ofSeq(liftedSeqOfExpr)
                  val liftedParts = Expr.ofSeq(parts)

                  '{
                    val str = interleave(
                      ${ liftedParts }.toList,
                      ${ liftedExprOfSeq }.toList
                    ).foldLeft("") { case (acc, e) =>
                      acc + java.util.Objects.toString(e) // handle nulls too
                    }
                    parse(str) match
                      case Left(exception) =>
                        throw Exception(
                          s"Failed to parse JSON:\n$str",
                          exception
                        )
                      case Right(value) =>
                        value
                  }
              end match
    end match
  end jsonImpl
