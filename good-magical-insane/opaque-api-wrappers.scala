//> using scala 3.3.1
//> using dep io.github.iltotore::iron:2.4.0

object opaqueapiwrappers:
  import io.github.iltotore.iron.*
  import io.github.iltotore.iron.constraint.numeric.*

  // only compiler knows this!
  opaque type File = java.io.File

  extension (f: File)
    def createNewFile(): Either[Exception, Boolean] =
      try Right(f.createNewFile())
      catch case e: Exception => Left(e)

    def setLastModified(time: Long :| Positive): Boolean =
      f.setLastModified(time)

  object File:
    def apply(path: String): File = java.io.File(path)

import opaqueapiwrappers.*
import io.github.iltotore.iron.*

@main def testSafeFile(): Unit =
  val file = File("test.txt")
  file.createNewFile() match
    case Right(true)  => println("File created")
    case Right(false) => println("File already exists")
    case Left(e)      => println(s"Path malformed: ${e.getMessage}")

  // file.setLastModified(0)
  // -- Constraint Error --------------------------------------------------------
  // Could not satisfy a constraint for type scala.Long.

  // Value: 0L
  // Message: Should be strictly positive
  // ----------------------------------------------------------------------------
