package lila.puzzle

import org.specs2.mutable._
import lila.puzzle.PuzzleApi

class PuzzleTest extends Specification {

  val testString = "test"
  val testBool = false

  private val db = new lila.db.Env("puzzle", config getConfig "mongodb", lifecycle)


  val puzzle1 = Puzzle(1, testString, null, testString, null, 1, null, null, null, null, 1, testBool)
  val puzzle2 = Puzzle(2, testString, null, testString, null, 1, null, null, null, null, 1, testBool)
  val puzzle3 = Puzzle(3, testString, null, testString, null, 1, null, null, null, null, 1, testBool)
  val puzzle4 = Puzzle(4, testString, null, testString, null, 1, null, null, null, null, 1, testBool)
  val puzzle5 = Puzzle(5, testString, null, testString, null, 1, null, null, null, null, 1, testBool)
  val puzzle6 = Puzzle(6, testString, null, testString, null, 1, null, null, null, null, 1, testBool)
  val puzzle7 = Puzzle(7, testString, null, testString, null, 1, null, null, null, null, 1, testBool)
  val puzzle8 = Puzzle(8, testString, null, testString, null, 1, null, null, null, null, 1, testBool)
  val puzzle9 = Puzzle(9, testString, null, testString, null, 1, null, null, null, null, 1, testBool)

  private[puzzle] lazy val puzzleColl = db(CollectionPuzzle)
  private[puzzle] lazy val puzzleMigrationColl = db(CollectionPuzzleMigration)

  lazy val api = new PuzzleApi(
    puzzleColl = puzzleColl,
    puzzleMigrationColl = puzzleColl,
    null, null, null, null, null, null)
}
