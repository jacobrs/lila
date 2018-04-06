package lila.puzzle

import com.typesafe.config.{ Config, ConfigFactory }
import lila.memo
import lila.memo.AsyncCache
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

  val testString = "test"
  val testBool = false
  var puzzleListBuffer = ListBuffer[Puzzle]()

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
      puzzleListBuffer += Puzzle(i, testString, null, testString, null, 1, null, null, null, null, 1, testBool)

    val puzzleList = puzzleListBuffer.toList

    for (puzzle <- puzzleList)
      api.puzzle.insertPuzzleToOld(puzzle)
  }

  def afterAll = Play.stop(application)

  "Api" should {
    "save same data to both tables" in {
      api.puzzle.forklift()

      0 must be equalTo api.puzzle.consistencyChecker()
    }

    "detect an inconsistency in the new database" in {
      api.puzzle.forklift()

      val inconsistentPuzzle = Puzzle.make(testString, null, null, null, List.empty[Line], testBool)(22);

      Await.result(api.puzzle.insertPuzzleToOld(inconsistentPuzzle), Duration.Inf)

      1 must be equalTo api.puzzle.consistencyChecker()
    }

    "detect shadow writing consistencies" in {
      val shadowPuzzle = Puzzle.make(testString, null, null, null, List.empty[Line], testBool)(23);
      api.puzzle.insertPuzzleToOldShadow(shadowPuzzle)

      0 must be equalTo api.puzzle.shadowWriteConsistencyChecker(shadowPuzzle)
    }

    "detect shadow writing inconsistencies" in {
      val shadowPuzzleInconsistent = Puzzle.make(testString, null, null, null, List.empty[Line], testBool)(24);
      api.puzzle.insertPuzzleToOld(shadowPuzzleInconsistent)

      1 must be equalTo api.puzzle.shadowWriteConsistencyChecker(shadowPuzzleInconsistent)
    }
  }
}
