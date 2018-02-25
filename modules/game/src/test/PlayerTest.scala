package lila.game

import chess.Color
import lila.game.Player.{ HoldAlert, UserInfo }
import lila.rating.Glicko
import lila.user.User
//import org.scalatest.FlatSpec
import org.mockito.Mockito._
import org.specs2.mutable._

class PlayerTest extends Specification {

  val playerId = "1111"
  val aiLevel: Option[Int] = Some(1)
  val human: Option[Int] = None

  //Mocks and Stubs
  //Color class
  val mockColor = mock(classOf[Color])

  //User class
  val mockUser = mock(classOf[User])
  when(mockUser.id).thenReturn("1234")

  //Glicko class
  var mockGlicko = mock(classOf[Glicko])
  when(mockGlicko.provisional).thenReturn(true)

  //Perf class
  var mockPerf = mock(classOf[lila.rating.Perf])
  when(mockPerf.glicko).thenReturn(mockGlicko)
  when(mockPerf.intRating).thenReturn(1337)

  //Tests
  "Players" should {
    "be an AI if declared as an AI" in {
      val player = new Player(playerId, mockColor, aiLevel)

      player.isAi must beTrue
      player.isHuman must beFalse
    }

    "be human if declared as human" in {
      val player = new Player(playerId, mockColor, human)

      player.isHuman must beTrue
      player.isAi must beFalse
    }

    /*
    "be a user and have a proper user id and rating" in {
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


    "return proper UserInfo object" in {
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

    "win the game" in {
      var player = new Player(playerId, mockColor, human)

      player = player.finish(true)
      assert(player.wins)
    }

    "go berserk" in {
      var player = new Player(playerId, mockColor, human)

      player = player.goBerserk

      assert(player.berserk)
    }

    "have a suspicious hold alert" in {
      val suspiciousHoldAlert = mock(classOf[HoldAlert])
      when(suspiciousHoldAlert.suspicious).thenReturn(true)

      var player = new Player(playerId, mockColor, human, None, false, false, None, 0, None, None, None, false, Blurs.blursZero.zero, Some(suspiciousHoldAlert))

      assert(player.hasSuspiciousHoldAlert)
      assert(player.hasHoldAlert)
    }

    "change draw variables when player offers a draw" in {
      var player = new Player(playerId, mockColor, human)

      player = player.offerDraw(2)

      assert(player.isOfferingDraw)
      assert(player.lastDrawOffer.contains(2))

    }
    */

    "change draw variables when player offers a draw or revmoves a draw offer" in {
      var player = new Player(playerId, mockColor, human)

      player = player.offerDraw(2)
      player.isOfferingDraw must beTrue

      player = player.removeDrawOffer
      player.isOfferingDraw must beFalse
    }

    "change rematch variables when player offers a rematch" in {
      var player = new Player(playerId, mockColor, human)

      player = player.offerRematch
      player.isOfferingRematch must beTrue
    }

    "change rematch variables when player removes a rematch offer" in {
      var player = new Player(playerId, mockColor, human)

      player = player.offerRematch
      player.isOfferingRematch must beTrue

      player = player.removeRematchOffer
      player.isOfferingRematch must beFalse
    }

    "propose a takeback and remove a takeback proposition" in {
      var player = new Player(playerId, mockColor, human)

      player = player.proposeTakeback(2)
      player.proposeTakebackAt must be equalTo (2)
      player.isProposingTakeback must beTrue

      player = player.removeTakebackProposition
      player.proposeTakebackAt must be equalTo (0)
      player.isProposingTakeback must beFalse

    }

    "return their currrent and after ratings" in {
      var player = new Player(playerId, mockColor, human,
        None, false, false, None, 0, None, None, Option(3))

      //Associate player with a mocked user
      player = player.withUser(mockUser.id, mockPerf)

      player.rating must be equalTo (Option(1337))
      player.ratingAfter must be equalTo (Option(1340))

    }

    //Incomplete below
    "return their specific rating depending on provisional" in {
      when(mockGlicko.provisional).thenReturn(true)
      var player = new Player(playerId, mockColor, human,
        None, false, false, None, 0, None, None, Option(3), mockGlicko.provisional)

      //Associate player with a mocked user
      player = player.withUser(mockUser.id, mockPerf)

      player.rating must be equalTo (Option(1337))

      //player.stableRating must be equalTo (None)

      //player.stableRatingAfter must be equalTo (Option(3))
    }

  }

}
