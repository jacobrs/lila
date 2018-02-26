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

  //Mocks
  //Color class
  val mockColor = mock(classOf[Color])

  //User class
  val mockUser = mock(classOf[User])

  //Glicko class
  val mockGlicko = mock(classOf[Glicko])

  //Perf class
  val mockPerf = mock(classOf[lila.rating.Perf])
  when(mockPerf.intRating).thenReturn(1337)

  //Tests
  "Players" should {
    "be an AI if declared as an AI" in {
      val player = Player(playerId, mockColor, aiLevel)

      player.isAi must beTrue
      player.isHuman must beFalse
    }

    "be human if declared as human" in {
      val player = Player(playerId, mockColor, human)

      player.isHuman must beTrue
      player.isAi must beFalse
    }

    "be a user and have a proper user id and rating" in {
      //mocking out the dependencies player needs to mark a player as a user
      val mockUser = mock(classOf[User])
      val mockPerf = mock(classOf[lila.rating.Perf])
      val mockGlicko = mock(classOf[Glicko])

      val playerTemp = Player(playerId, mockColor, human)
      val playerRating = 1

      //stubbing out the mocked methods called in the player method
      when(mockUser.id).thenReturn(playerId)
      when(mockPerf.intRating).thenReturn(playerRating)
      when(mockPerf.glicko).thenReturn(mockGlicko)
      when(mockGlicko.provisional).thenReturn(true)

      val player = playerTemp.withUser(playerId, mockPerf)

      //verify the stubs were called
      verify(mockPerf).intRating
      verify(mockGlicko).provisional

      //assert that the player object has changed
      player.userId must be equalTo Option(playerId)
      player.rating must be equalTo Option(playerRating)
      player.provisional must beTrue

      player.isUser(mockUser) must beTrue
      player.hasUser must beTrue
    }

    "return proper UserInfo object" in {
      val player = Player(playerId, mockColor, human)

      val mockUser = mock(classOf[User])
      val mockPerf = mock(classOf[lila.rating.Perf])
      val mockGlicko = mock(classOf[Glicko])

      val playerRating = 1

      //stubbing out the mocked methods called in the player method
      when(mockUser.id).thenReturn(playerId)
      when(mockPerf.intRating).thenReturn(playerRating)
      when(mockPerf.glicko).thenReturn(mockGlicko)
      when(mockGlicko.provisional).thenReturn(true)

      val userPlayer = player.withUser(playerId, mockPerf)

      val userInfo = userPlayer.userInfos.get

      userInfo.id must be equalTo (userPlayer.userId.get)
      userInfo.rating must be equalTo (userPlayer.rating.get)
      userInfo.provisional must be equalTo (userPlayer.provisional)
    }

    "win the game" in {
      val player1Temp = Player(playerId, mockColor, human)
      val player2 = Player((playerId + 1), mockColor, human)

      val player1 = player1Temp.finish(true)
      player1.wins must beTrue
      player2.wins must beFalse

    }

    "go berserk" in {
      val playerTemp = Player(playerId, mockColor, human)

      val player = playerTemp.goBerserk

      player.berserk must beTrue
    }

    "have a suspicious hold alert" in {
      val suspiciousHoldAlert = mock(classOf[HoldAlert])
      when(suspiciousHoldAlert.suspicious).thenReturn(true)

      val player = Player(id = playerId, color = mockColor, aiLevel = human, blurs = Blurs.blursZero.zero, holdAlert = Some(suspiciousHoldAlert))

      player.hasSuspiciousHoldAlert must beTrue
      player.hasHoldAlert must beTrue
    }

    "change draw variables when player offers a draw or revmoves a draw offer" in {
      val playerTemp = Player(playerId, mockColor, human)

      val playerDraw = playerTemp.offerDraw(2)
      playerDraw.isOfferingDraw must beTrue

      val playerNoDraw = playerDraw.removeDrawOffer
      playerNoDraw.isOfferingDraw must beFalse
    }

    "change rematch variables when player offers a rematch or removes a rematch offer" in {
      val playerTemp = Player(playerId, mockColor, human)

      val playerRematch = playerTemp.offerRematch
      playerRematch.isOfferingRematch must beTrue

      val playerNoRematch = playerRematch.removeRematchOffer
      playerNoRematch.isOfferingRematch must beFalse
    }

    "propose a takeback and remove a takeback proposition" in {
      val playerTemp = Player(playerId, mockColor, human)

      val playerTakeback = playerTemp.proposeTakeback(2)
      playerTakeback.proposeTakebackAt must be equalTo (2)
      playerTakeback.isProposingTakeback must beTrue

      val playerNoTakeback = playerTakeback.removeTakebackProposition
      playerNoTakeback.proposeTakebackAt must be equalTo (0)
      playerNoTakeback.isProposingTakeback must beFalse

    }

    "be able to edit their names" in {
      //stubs
      when(mockGlicko.provisional).thenReturn(false)
      when(mockUser.id).thenReturn("1234")
      when(mockPerf.glicko).thenReturn(mockGlicko)
      when(mockUser.username).thenReturn("MockedUsername")

      val playerTemp = Player(playerId, mockColor, human)
      val playerUser = playerTemp.withUser(mockUser.id, mockPerf)

      val playerNamed = playerUser.withName(mockUser.username)
      playerNamed.name must beSome[String]("MockedUsername")
    }

    "return their currrent and after ratings" in {
      //stubs
      when(mockGlicko.provisional).thenReturn(false)
      when(mockUser.id).thenReturn("1234")
      when(mockPerf.glicko).thenReturn(mockGlicko)

      val playerTemp = Player(id = playerId, color = mockColor, aiLevel = human, ratingDiff = Option(3))

      //Associate player with a mocked user
      val player = playerTemp.withUser(mockUser.id, mockPerf)

      player.rating must be equalTo (Option(1337))
      player.ratingAfter must be equalTo (Option(1340))

    }

    "return their stable ratings" in {
      //Stubs
      //When provisional set to false
      when(mockGlicko.provisional).thenReturn(false)
      when(mockUser.id).thenReturn("1234")
      when(mockPerf.glicko).thenReturn(mockGlicko)
      when(mockPerf.intRating).thenReturn(1337)

      val player1Temp = Player(id = playerId, color = mockColor, aiLevel = human,
        ratingDiff = Option(3), provisional = mockGlicko.provisional)

      //Associate player with a mocked user
      val player1 = player1Temp.withUser(mockUser.id, mockPerf)

      player1.rating must be equalTo (Option(1337))
      player1.stableRating must beSome[Int](1337)
      player1.stableRatingAfter must beSome[Int](1340)

      //Changed stubs
      //When provisional set to true
      when(mockGlicko.provisional).thenReturn(true)
      when(mockUser.id).thenReturn("5678")
      when(mockPerf.glicko).thenReturn(mockGlicko)

      val player2Temp = Player(id = playerId, color = mockColor, aiLevel = human,
        ratingDiff = Option(3), provisional = mockGlicko.provisional)

      //Associate player with a mocked user
      val player2 = player2Temp.withUser(mockUser.id, mockPerf)

      player2.rating must be equalTo (Option(1337))
      player2.stableRating must be equalTo (None)
      player2.stableRatingAfter must be equalTo (None)

    }

  }

}
