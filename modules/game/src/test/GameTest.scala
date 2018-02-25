package lila.game

import chess.Color.{ Black, White }
import chess.{ Color, Status, UnmovedRooks }
import lila.db.ByteArray
import lila.game.Source.{ Import, Lobby }
import org.mockito.Mockito._
import org.specs2.mutable._

class GameTest extends Specification {

  // Mock Player class
  private val mockWhitePlayer = mock(classOf[Player])
  private val mockBlackPlayer = mock(classOf[Player])

  // Mock Metadata class (parameter for new Game)
  private val defaultMockMetadata = mock(classOf[Metadata])

  // ByteArray instances (parameter for new Game)
  val byteArray1 = ByteArray(Array())
  val byteArray2 = ByteArray(Array())

  // UnmovedRooks class (parameter for new Game)
  private val mockUnmovedRooks = UnmovedRooks(Set())

  when(mockWhitePlayer.color).thenReturn(White)
  when(mockBlackPlayer.color).thenReturn(Black)

  var gameId = "22222"

  def getGameInstance(metadata: Metadata = defaultMockMetadata, whitePlayer: Player = mockWhitePlayer,
    blackPlayer: Player = mockBlackPlayer, status: Status = Status.Created): Game =
    Game(id = gameId, whitePlayer = whitePlayer, blackPlayer = blackPlayer, binaryPgn = byteArray1,
      binaryPieces = byteArray2, turns = 0, startedAtTurn = 0, status = status, castleLastMoveTime = null,
      unmovedRooks = mockUnmovedRooks, metadata = metadata, daysPerTurn = Some(1))

  "Players" should {

    "have a defining color" in {
      val game = getGameInstance()

      mockWhitePlayer.color must be(White)
      mockBlackPlayer.color must be(Black)
      game.player(White) must be(mockWhitePlayer)
      game.player(Black) must be(mockBlackPlayer)
    }
  }

  "Game" should {

    "be tournament if tournament Id is set" in {
      val mockMetadata = mock(classOf[Metadata])
      val game = getGameInstance(mockMetadata)

      when(mockMetadata.tournamentId).thenReturn(None)
      game.isTournament must beFalse
      when(mockMetadata.tournamentId).thenReturn(Some("123"))
      game.isTournament must beTrue
      game.hasChat must beFalse
    }

    "be simul if simulId is set" in {
      val mockMetadata = mock(classOf[Metadata])
      val game = getGameInstance(mockMetadata)

      when(mockMetadata.tournamentId).thenReturn(None)
      when(mockMetadata.simulId).thenReturn(None)
      game.isSimul must beFalse
      when(mockMetadata.tournamentId).thenReturn(None)
      when(mockMetadata.simulId).thenReturn(Some("456"))
      game.isSimul must beTrue
      game.hasChat must beFalse
    }

    "be mandatory if either a tournament or simul is defined" in {
      val mockMetadata = mock(classOf[Metadata])
      val game = getGameInstance(mockMetadata)

      when(mockMetadata.tournamentId).thenReturn(None)
      when(mockMetadata.simulId).thenReturn(None)
      game.isMandatory must beFalse
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

    "have white as start color" in {
      val game = getGameInstance()
      mockWhitePlayer.color must be(chess.Color.White)
      game.startColor must be(chess.Color.White)
    }

    "be playable by default" in {
      val lobbyMetadata = mock(classOf[Metadata])
      when(lobbyMetadata.source).thenReturn(Some(Lobby))

      val game = getGameInstance(lobbyMetadata)
      game.fromLobby must beTrue
      game.playable must beTrue
    }

    "not be playable if imported or aborted" in {
      val importedMetadata = mock(classOf[Metadata])
      when(importedMetadata.source).thenReturn(Some(Import))

      val importedGame = getGameInstance(importedMetadata)

      importedGame.imported must beTrue
      importedGame.playable must beFalse
      getGameInstance(status = Status.Aborted).playable must beFalse
    }

    "return correct player and opponent values" in {
      val game = getGameInstance()
      game.player must be(mockWhitePlayer)
      game.opponent(game.player) must be(mockBlackPlayer)
      game.opponent(mockWhitePlayer) must be(mockBlackPlayer)
      game.opponent(mockBlackPlayer) must be(mockWhitePlayer)
    }

    "return the right amount of turns played" in {
      val game = spy(getGameInstance())
      when(game.startedAtTurn).thenReturn(0)
      when(game.turns).thenReturn(10)
      game.playedTurns must beEqualTo(10)

      when(game.startedAtTurn).thenReturn(5)
      game.playedTurns must beEqualTo(5)
    }

    "flag games that are out of time" in {
      val game = spy(getGameInstance())
      game.flagged must be(None)

      when(game.status).thenReturn(Status.Outoftime)
      game.flagged must beSome[Color](White)
    }

    "consider calculate proper average rating" in {
      val weakPlayer = mock(classOf[Player])
      val strongPlayer = mock(classOf[Player])

      when(weakPlayer.rating).thenReturn(Some(1000))
      when(strongPlayer.rating).thenReturn(Some(2000))

      val game = spy(getGameInstance(whitePlayer = weakPlayer, blackPlayer = strongPlayer))
      game.whitePlayer must be(weakPlayer)
      game.blackPlayer must be(strongPlayer)
      game.players must beEqualTo(List(weakPlayer, strongPlayer))
      game.averageUsersRating must beSome[Int](1500)
    }
  }
}
