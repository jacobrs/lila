import lila.rating.RatingRange
import org.specs2.mutable._

class RatingRangeTest extends Specification {
  "RatingRange" should {
    "set a proper default" in {
      val rr = RatingRange(1000, 2000);

      rr.toString must beEqualTo("1000-2000")
    }

    "properly contain a value within its range" in {
      val rr = RatingRange(1499, 1501);
      rr.contains(1500) must beTrue
    }

    "properly exclude a values out of its range" in {
      val rr = RatingRange(1499, 1501);
      rr.contains(1498) must beFalse
      rr.contains(1502) must beFalse
    }

    "ignore the min if it passes the RatingRange min" in {
      val rr = RatingRange(200, 1501);
      rr.contains(100) must beTrue
    }

    "ignore the max if it passes the RatingRange max" in {
      val rr = RatingRange(1499, 3000);
      rr.contains(1000000) must beTrue
    }

    "ignore the min and max if they pass their respective RatingRange values" in {
      val rr = RatingRange(100, 3000);
      rr.contains(-1000000) must beTrue
      rr.contains(1000000) must beTrue
    }
  }
}
