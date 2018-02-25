package lila.game

import chess.Color.{ Black, White }
import chess.{ Status, UnmovedRooks }
import lila.db.ByteArray
import lila.game.Source.Lobby
import org.mockito.Mockito._
import org.specs2.mutable._

class GameTest extends Specification {

  // Mock Player class
  private val mockWhitePlayer = mock(classOf[Player])
  private val mockBlackPlayer = mock(classOf[Player])

  // Mock Metadata class (parameter for new Game)
  private val defaultMockMetadata = mock(classOf[Metadata])

  // ByteArray instances (parameter for new Game)
  // NOTE: Could not mock ByteArray, received error saying unable to mock static/final classes. Will look into it
  val byteArray1 = new ByteArray(Array())
  val byteArray2 = new ByteArray(Array())

  // Mock UnmovedRooks class (parameter for new Game)
  private val mockUnmovedRooks = UnmovedRooks(Set())

  // Color values
  private val white = White
  private val black = Black

  when(mockWhitePlayer.color).thenReturn(white)
  when(mockBlackPlayer.color).thenReturn(black)

  // Other
  var gameId = "22222"

  def getGameInstance(metadata: Metadata, whitePlayer: Player = mockWhitePlayer): Game =
    Game(id = gameId, whitePlayer = mockWhitePlayer, blackPlayer = mockBlackPlayer, binaryPgn = byteArray1,
      binaryPieces = byteArray2, turns = 0, startedAtTurn = 0, status = Status.Created, castleLastMoveTime = null,
      unmovedRooks = mockUnmovedRooks, metadata = metadata, daysPerTurn = Some(1))

  //-----------------
  //     TESTS
  //-----------------

  // player(color: Color)
  "Players" should {
    "have a defining color" in {
      val game = getGameInstance(defaultMockMetadata)

      game.player(white) must be(mockWhitePlayer)
      game.player(black) must be(mockBlackPlayer)
    }
  }

  // opponent(p: Player), opponent(c: Color)
  "Opponent" should {
    "have opposite color" in {
      val mockPlayer = mock(classOf[Player])
      val game = getGameInstance(defaultMockMetadata, mockPlayer)

      when(mockPlayer.color).thenReturn(white)
      game.opponent(mockPlayer) must be(mockBlackPlayer)
    }
  }

  "Game" should {

    "be tournament if tournament Id is set" in {
      val mockMetadata = mock(classOf[Metadata])
      val game = getGameInstance(mockMetadata)

      when(mockMetadata.tournamentId).thenReturn(Some("123"))
      game.isTournament must beTrue
      game.hasChat must beFalse
    }

    "be simul if simulId is set" in {
      val mockMetadata = mock(classOf[Metadata])
      val game = getGameInstance(mockMetadata)

      when(mockMetadata.tournamentId).thenReturn(None)
      when(mockMetadata.simulId).thenReturn(Some("456"))
      game.isSimul must beTrue
      game.hasChat must beFalse
    }

    "be mandatory if either a tournament or simul is defined" in {
      val mockMetadata = mock(classOf[Metadata])
      val game = getGameInstance(mockMetadata)

      when(mockMetadata.tournamentId).thenReturn(Some("123"))
      when(mockMetadata.simulId).thenReturn(Some("456"))
      game.isMandatory must beTrue
      game.nonMandatory must beFalse
      game.hasChat must beFalse
    }

    "have a chat if non tourney, non simul, and no AI" in {
      val mockMetadata = mock(classOf[Metadata])
      val game = getGameInstance(mockMetadata)

      when(mockMetadata.tournamentId).thenReturn(None)
      when(mockMetadata.simulId).thenReturn(None)
      when(mockWhitePlayer.isAi).thenReturn(false)
      when(mockBlackPlayer.isAi).thenReturn(false)
      game.hasAi must beFalse
      game.nonAi must beTrue
      game.hasChat must beTrue
    }

    "should have white as start color" in {
      val game = getGameInstance(defaultMockMetadata)
      mockWhitePlayer.color must be(chess.Color.White)
      game.startColor must be(chess.Color.White)
    }

    "should be playable by default" in {
      val lobbyMetadata = mock(classOf[Metadata])
      when(lobbyMetadata.source).thenReturn(Some(Lobby))

      val game = getGameInstance(lobbyMetadata)
      game.playable must beTrue
    }
  }
}
