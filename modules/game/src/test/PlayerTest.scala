package lila.game

import chess.Color
import lila.game.Player.{ HoldAlert, UserInfo }
import lila.rating.Glicko
import lila.user.User
import org.scalatest.FlatSpec
import org.mockito.Mockito._

class PlayerTest extends FlatSpec {

  val playerId = "1111"
  val aiLevel: Option[Int] = Some(1)
  val human: Option[Int] = None
  val mockColor = mock(classOf[Color])

  "A Player" should "be ai" in {
    val player = new Player(playerId, mockColor, aiLevel)

    assert(player.isAi)
    assert(!player.isHuman)
  }
  it should "be human" in {
    val player = new Player(playerId, mockColor, human)

    assert(player.isHuman)
    assert(!player.isAi)
  }

  it should "be a user and have proper user id and rating" in {

    //mocking out the dependencies player needs to mark a player as a user
    val mockUser = mock(classOf[User])
    var mockPerf = mock(classOf[lila.rating.Perf])
    val mockGlicko = mock(classOf[Glicko])

    var player = new Player(playerId, mockColor, human)
    var playerRating = 1

    //stubbing out the mocked methods called in the player method
    when(mockUser.id).thenReturn(playerId)
    when(mockPerf.intRating).thenReturn(playerRating)
    when(mockPerf.glicko).thenReturn(mockGlicko)
    when(mockGlicko.provisional).thenReturn(true)

    player = player.withUser(playerId, mockPerf)

    //verify the stubs were called
    verify(mockPerf).intRating
    verify(mockGlicko).provisional

    //assert that the player object has changed
    assert(player.userId.contains(playerId))
    assert(player.rating.contains(playerRating))
    assert(player.provisional)

    assert(player.isUser(mockUser))
    assert(player.hasUser)
  }

  it should "return proper UserInfo object" in {
    var player = new Player(playerId, mockColor, human)

    val mockUser = mock(classOf[User])
    var mockPerf = mock(classOf[lila.rating.Perf])
    val mockGlicko = mock(classOf[Glicko])

    var playerRating = 1

    //stubbing out the mocked methods called in the player method
    when(mockUser.id).thenReturn(playerId)
    when(mockPerf.intRating).thenReturn(playerRating)
    when(mockPerf.glicko).thenReturn(mockGlicko)
    when(mockGlicko.provisional).thenReturn(true)

    player = player.withUser(playerId, mockPerf)

    val userInfo = player.userInfos.get

    assert(userInfo.id == player.userId.get)
    assert(userInfo.rating == player.rating.get)
    assert(userInfo.provisional == player.provisional)

  }

  it should "win the game" in {
    var player = new Player(playerId, mockColor, human)

    player = player.finish(true)
    assert(player.wins)
  }

  it should "Go berserk" in {
    var player = new Player(playerId, mockColor, human)

    player = player.goBerserk

    assert(player.berserk)
  }

  it should "have a suspicious hold alert" in {
    val suspiciousHoldAlert = mock(classOf[HoldAlert])
    when(suspiciousHoldAlert.suspicious).thenReturn(true)

    var player = new Player(playerId, mockColor, human, None, false, false, None, 0, None, None, None, false, Blurs.blursZero.zero, Some(suspiciousHoldAlert))

    assert(player.hasSuspiciousHoldAlert)
    assert(player.hasHoldAlert)
  }

  it should "change draw variables when player offers a draw" in {
    var player = new Player(playerId, mockColor, human)

    player = player.offerDraw(2)

    assert(player.isOfferingDraw)
    assert(player.lastDrawOffer.contains(2))

  }
}
