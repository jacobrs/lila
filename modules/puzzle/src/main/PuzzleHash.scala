package lila.puzzle

import lila.db.BSON
import reactivemongo.bson.BSONDocument

case class PuzzleHash(
    id: PuzzleId,
    hash: String
)

object PuzzleHash {
  object BSONFields {
    val id = "_id"
    val hash = "hash"
  }

  implicit val puzzleHashBSONHandler: BSON[PuzzleHash] = new BSON[PuzzleHash] {

    import BSONFields._

    def reads(r: BSON.Reader): PuzzleHash = PuzzleHash(
      id = r int id,
      hash = r str hash
    )

    def writes(w: BSON.Writer, o: PuzzleHash) = BSONDocument(
      id -> o.id,
      hash -> o.hash
    )
  }
}