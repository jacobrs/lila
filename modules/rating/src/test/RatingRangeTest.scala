import lila.rating.RatingRange
import org.specs2.mutable._

class RatingRangeTest extends Specification {
  "RatingRange" should {
    "set a proper default" in {
      val rr = new RatingRange(1000, 2000);

      rr.toString must_== s"1000-2000"
    }

    "properly contain a value within its range" in {
      val rr = new RatingRange(1499, 1501);
      rr.contains(1500) must_== true
    }

    "properly exclude a values out of its range" in {
      val rr = new RatingRange(1499, 1501);
      rr.contains(1498) must_== false
      rr.contains(1502) must_== false
    }

    "ignore the min if it passes the RatingRange min" in {
      val rr = new RatingRange(200, 1501);
      rr.contains(100) must_== true
    }

    "ignore the max if it passes the RatingRange max" in {
      val rr = new RatingRange(1499, 3000);
      rr.contains(1000000) must_== true
    }

    "ignore the min and max if they pass their respective RatingRange values" in {
      val rr = new RatingRange(100, 3000);
      rr.contains(-1000000) must_== true
      rr.contains(1000000) must_== true
    }
  }
}
