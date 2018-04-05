package lila.puzzle

import com.typesafe.config.{ Config, ConfigFactory }
import org.specs2.mutable._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api._

class PuzzleTest extends Specification {

  var application: Application = _
  var db: Env = _

  step {
    application = new GuiceApplicationBuilder().loadConfig(
      Configuration(ConfigFactory.load("base.conf"))
    ).in(Mode.Test).build()
    Logger.logger.debug(ConfigFactory.load("base.conf").getString("mongodb.uri"))
    Play.start(application)
    db = lila.puzzle.Env.current
  }

  step {
    val config: Config = ConfigFactory.load("base.conf")

    val testString = "test"
    val testBool = false

    val puzzle1 = Puzzle(1, testString, null, testString, null, 1, null, null, null, null, 1, testBool)
    val puzzle2 = Puzzle(2, testString, null, testString, null, 1, null, null, null, null, 1, testBool)
    val puzzle3 = Puzzle(3, testString, null, testString, null, 1, null, null, null, null, 1, testBool)
    val puzzle4 = Puzzle(4, testString, null, testString, null, 1, null, null, null, null, 1, testBool)
    val puzzle5 = Puzzle(5, testString, null, testString, null, 1, null, null, null, null, 1, testBool)
    val puzzle6 = Puzzle(6, testString, null, testString, null, 1, null, null, null, null, 1, testBool)
    val puzzle7 = Puzzle(7, testString, null, testString, null, 1, null, null, null, null, 1, testBool)
    val puzzle8 = Puzzle(8, testString, null, testString, null, 1, null, null, null, null, 1, testBool)
    val puzzle9 = Puzzle(9, testString, null, testString, null, 1, null, null, null, null, 1, testBool)

    lazy val api = new PuzzleApi(
      puzzleColl = db.puzzleColl,
      puzzleMigrationColl = db.puzzleMigrationColl,
      null, null, null, Predef.Integer2int(null), null, null
    )

    "the api" should {
      "not be null" in {
        api shouldNotEqual null
      }
    }
  }

  step {
    Play.stop(application)
  }
}
