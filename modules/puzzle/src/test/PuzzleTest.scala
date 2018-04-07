package lila.puzzle

import chess.Color
import com.typesafe.config.{ Config, ConfigFactory }
import lila.memo
import lila.memo.AsyncCache
import org.joda.time.DateTime
import org.specs2.mutable._
import org.specs2.specification
import org.specs2.specification.{ AfterAll, BeforeAll }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api._

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, ExecutionContextExecutor }

class PuzzleTest extends SpecificationLike with BeforeAll with AfterAll {

  implicit var ec: ExecutionContext = _
  var application: Application = _
  var db: Env = _
  var testColor: Color = Color.white
  var testDate: DateTime = _
  var testPerf: PuzzlePerf = _
  var testAggregateVote: AggregateVote = _
  var testLine: Line = _

  val testString = "test"
  val testBool = false
  var puzzleListBuffer = ListBuffer[Puzzle]()
  var testLines = List[Line](testLine)
  var testHistory = List[String](testString)

  lazy val api = new PuzzleApi(
    puzzleColl = db.puzzleColl,
    puzzleMigrationColl = db.puzzleMigrationColl,
    db.roundColl,
    db.voteColl,
    db.headColl,
    db.puzzleIdMin,
    db.publicAsyncCache,
    db.settings.ApiToken
  )

  def beforeAll = {
    application = new GuiceApplicationBuilder().loadConfig(
      Configuration(ConfigFactory.load("base.conf"))
    ).in(Mode.Test).build()
    Logger.logger.debug(ConfigFactory.load("base.conf").getString("mongodb.uri"))
    Play.start(application)
    db = lila.puzzle.Env.current
    ec = application.injector.instanceOf[ExecutionContext]

    for (i <- 1 to 9)
      puzzleListBuffer += Puzzle(i, testString, testHistory, testString, testLines, 1, testColor, testDate, testPerf, testAggregateVote, 1, testBool)

    val puzzleList = puzzleListBuffer.toList

    for (puzzle <- puzzleList)
      api.puzzle.insertPuzzleToOld(puzzle)
  }

  def afterAll = Play.stop(application)

  "Api" should {
    "save same data to both tables" in {
      api.puzzle.forklift()

      api.puzzle.consistencyChecker() must be equalTo 0
    }

    "detect an inconsistency in the new database" in {
      val inconsistentPuzzle = Puzzle.make(testString, testHistory, testString, testColor, List.empty[Line], testBool)(10);

      Await.result(api.puzzle.insertPuzzleToOld(inconsistentPuzzle), Duration.Inf)

      api.puzzle.consistencyChecker() must not equalTo 0
    }

    "detect shadow writing consistencies" in {
      val shadowPuzzle = Puzzle.make(testString, testHistory, testString, testColor, List.empty[Line], testBool)(11);

      Await.result(api.puzzle.insertPuzzleToOldShadow(shadowPuzzle), Duration.Inf)

      api.puzzle.shadowWriteConsistencyChecker(shadowPuzzle) must be equalTo 0
    }

    "detect shadow writing inconsistencies" in {
      val shadowPuzzleInconsistent = Puzzle.make(testString, testHistory, testString, testColor, List.empty[Line], testBool)(12);

      Await.result(api.puzzle.insertPuzzleToOld(shadowPuzzleInconsistent), Duration.Inf)

      api.puzzle.shadowWriteConsistencyChecker(shadowPuzzleInconsistent) must be equalTo 1
    }

    "detect shadow read consistencies" in {
      val shadowPuzzle = Puzzle.make(testString, testHistory, testString, testColor, List.empty[Line], testBool)(13);

      Await.result(api.puzzle.insertPuzzleToOldShadow(shadowPuzzle), Duration.Inf)

      api.puzzle.shadowReadConsistencyChecker(13) must be equalTo 0
    }

    "detect shadow read inconsistencies" in {
      val shadowPuzzleInconsistent = Puzzle.make(testString, testHistory, testString, testColor, List.empty[Line], testBool)(14);

      Await.result(api.puzzle.insertPuzzleToOld(shadowPuzzleInconsistent), Duration.Inf)

      api.puzzle.shadowReadConsistencyChecker(14) must be equalTo 1
    }
  }
}
