package lila.game

import chess.Color
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
}
