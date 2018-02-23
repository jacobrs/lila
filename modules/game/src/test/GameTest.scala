package lila.game

import lila.db.ByteArray
import chess.Color
import chess.UnmovedRooks
import org.mockito.Mockito._
import org.specs2.mutable._

class GameTest extends Specification {

  // Mock Player class
  private val mockPlayer = mock(classOf[Player])
  private val mockWhitePlayer = mock(classOf[Player])
  private val mockBlackPlayer = mock(classOf[Player])

  // Mock Metadata class (parameter for new Game)
  private val mockMetadata = mock(classOf[Metadata])

  // ByteArray instances (parameter for new Game)
  // NOTE: Could not mock ByteArray, received error saying unable to mock static/final classes. Will look into it
  val byteArray1 = new ByteArray(Array())
  val byteArray2 = new ByteArray(Array())

  // Mock UnmovedRooks class (parameter for new Game)
  private val mockUnmovedRooks = new UnmovedRooks(Set())

  // Color values
  private val white = Color.White
  private val black = Color.Black

  // Other
  var gameId = "22222"

  // Game instance
  var game = new Game(
    gameId,
    mockWhitePlayer,
    mockBlackPlayer,
    byteArray1,
    byteArray2,
    null,
    0,
    0,
    null,
    null,
    mockUnmovedRooks,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    0,
    null,
    null,
    mockMetadata
  )

  //-----------------
  //     TESTS
  //-----------------

  // player(color: Color)
  "Player" should {
    "have a defining color" in {
      game.player(white) must be(mockWhitePlayer)
      game.player(black) must be(mockBlackPlayer)
    }
  }

  // opponent(p: Player), opponent(c: Color)
  "Opponent" should {
    "have opposite color" in {
      when(mockPlayer.color).thenReturn(white)
      game.opponent(mockPlayer).color must be(Color.Black)
    }
  }

  // tournamentId, isTournament
  "Game" should {
    "be tournament" in {
      when(mockMetadata.tournamentId).thenReturn(Some("123"))
      game.isTournament must beTrue
    }
  }

  // simulId, isSimul
  "Simul" should {
    "be simul" in {
      when(mockMetadata.simulId).thenReturn(Some("456"))
      game.isSimul must beTrue
    }
  }

  // isMandatory, nonMandatory
  "Simul" should {
    "be mandatory if either a tournament or simul is defined" in {
      when(mockMetadata.tournamentId).thenReturn(Some("123"))
      game.isMandatory must beTrue
      game.nonMandatory must beFalse
    }
  }

  // hasChat, hasAi, nonAi
  "hasChat" should {
    "have a chat if non tourney, non simul, and no AI" in {
      when(mockWhitePlayer.isAi).thenReturn(false)
      when(mockBlackPlayer.isAi).thenReturn(false)
      game.hasAi must beFalse
      game.nonAi must beTrue
      game.hasChat must beTrue
    }
  }
}
