package lila.game

import org.specs2.mutable._
import org.mockito.Mockito._

import chess.{ Centis, PromotableRole, Pos, Color, Situation, Move => ChessMove, Drop => ChessDrop, Clock => ChessClock, Status }
import JsonView._
import play.api.libs.json.JsNull
import play.api.libs.json.JsUndefined

class EventTest extends Specification {

  //mocking objects to be used later
  private val mockRole = mock(classOf[PromotableRole])
  private val mockPos = mock(classOf[Pos])
  private val mockColor = mock(classOf[Color])
  //setting return values for objects
  val posKey = "67"
  when(mockPos.key).thenReturn(posKey)
  val roleName = "queen"
  when(mockRole.toString).thenReturn(roleName)

  "Promotion event" should {
    val event = Event.Promotion(mockRole, mockPos)

    "contain proper json structure as data" in {
      (event.data \ "key").as[String] must beEqualTo(posKey)
      (event.data \ "pieceClass").as[String] must beEqualTo(roleName)
    }

    "have type be promotion" in {
      event.typ must beEqualTo("promotion")
    }
  }

  "Enpassant event" should {
    val event = Event.Enpassant(mockPos, mockColor)

    "contain proper json structure as data" in {
      (event.data \ "key").as[String] must beEqualTo(posKey)
      event.data.keys.contains("color") must beTrue
    }

    "have type be enpassant" in {
      event.typ must beEqualTo("enpassant")
    }
  }

  "Castling event" should {
    val event = Event.Castling((mockPos, mockPos), (mockPos, mockPos), mockColor)

    "contain proper json structure as data" in {
      (event.data \ "king").as[List[String]] must beEqualTo(List(posKey, posKey))
      (event.data \ "rook").as[List[String]] must beEqualTo(List(posKey, posKey))
      event.data.keys.contains("color") must beTrue
    }

    "have type be castling" in {
      event.typ must beEqualTo("castling")
    }
  }

  "TakebackOffers event" should {
    val event = Event.TakebackOffers(true, false)

    "contain proper json structure as data" in {
      (event.data \ "white").as[Boolean] must beTrue
    }

    "have type be TakebackOffers" in {
      event.typ must beEqualTo("takebackOffers")
    }

    "have owner" in {
      event.owner must beTrue
    }
  }

  "CheckCount event" should {
    val event = Event.CheckCount(1, 2)

    "contain proper json structure as data" in {
      (event.data \ "white").as[Int] must beEqualTo(1)
      (event.data \ "black").as[Int] must beEqualTo(2)
    }

    "have type be checkCount" in {
      event.typ must beEqualTo("checkCount")
    }
  }

  "CheckCount event" should {
    val event = Event.CorrespondenceClock(1.1f, 2.2f)

    "contain proper json structure as data" in {
      (event.data \ "white").as[Float] must beEqualTo(1.1f)
      (event.data \ "black").as[Float] must beEqualTo(2.2f)
    }

    "have type be cclock" in {
      event.typ must beEqualTo("cclock")
    }
  }
  "Start event" should {
    val startEvent = Event.Start

    "contain null data" in {
      startEvent.data must beEqualTo(JsNull)
    }

    "have type be start" in {
      startEvent.typ must beEqualTo("start")
    }
  }

  "Premove event" should {
    val premoveEvent = Event.Premove(mockColor)

    "contain null data" in {
      premoveEvent.data must beEqualTo(JsNull)
    }

    "have type be premove" in {
      premoveEvent.typ must beEqualTo("premove")
    }
  }

  "Reload event" should {
    val reloadEvent = Event.Reload

    "contain null data" in {
      reloadEvent.data must beEqualTo(JsNull)
    }

    "have type be reload" in {
      reloadEvent.typ must beEqualTo("reload")
    }
  }

  "Reload Owner event" should {
    val reloadEvent = Event.ReloadOwner

    "contain null data" in {
      reloadEvent.data must beEqualTo(JsNull)
    }

    "have type be reload" in {
      reloadEvent.typ must beEqualTo("reload")
    }
    "have owner" in {
      reloadEvent.owner must beTrue
    }

  }
}
