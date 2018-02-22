package lila.game

import lila.db.ByteArray
import chess.Color
import chess.UnmovedRooks
import org.scalatest.FlatSpec
import org.mockito.Mockito._

class GameTest extends FlatSpec {

  // Mock Player class
  var mockPlayer = mock(classOf[Player])
  var mockWhitePlayer = mock(classOf[Player])
  var mockBlackPlayer = mock(classOf[Player])

  // Mock Metadata class (parameter for new Game)
  var mockMetadata = mock(classOf[Metadata])

  // ByteArray instances (parameter for new Game)
  // NOTE: Could not mock ByteArray, received error saying unable to mock static/final classes. Will look into it
  var byteArray1 = new ByteArray(Array())
  var byteArray2 = new ByteArray(Array())

  // Mock UnmovedRooks class (parameter for new Game)
  var mockUnmovedRooks = mock(classOf[UnmovedRooks])

  // Color values
  var white = Color.White
  var black = Color.Black

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
  "Player" should "have a defining color" in {
    assert(game.player(white) == mockWhitePlayer)
    assert(game.player(black) == mockBlackPlayer)
  }

  // opponent(p: Player), opponent(c: Color)
  "Opponent" should "have opposite color" in {
    when(mockPlayer.color).thenReturn(white)
    verify(game.opponent(mockPlayer).color == Color.Black)
  }

  // tournamentId, isTournament
  "Game" should "be tournament" in {
    when(mockMetadata.tournamentId).thenReturn(Some("123"))
    assert(game.isTournament)
  }

  // simulId, isSimul
  it should "be simul" in {
    when(mockMetadata.simulId).thenReturn(Some("456"))
    assert(game.isSimul)
  }

  // isMandatory, nonMandatory
  it should "be mandatory if either a tournament or simul is defined" in {
    when(mockMetadata.tournamentId).thenReturn(Some("123"))
    assert(game.isMandatory == true)
    assert(game.nonMandatory == false)
  }

  // hasChat, hasAi, nonAi
  it should "have a chat if non tourney, non simul, and no AI" in {
    when(mockWhitePlayer.isAi).thenReturn(false)
    when(mockBlackPlayer.isAi).thenReturn(false)
    assert(game.hasAi == false)
    assert(game.nonAi == true)
    assert(game.hasChat == true)
  }
}
