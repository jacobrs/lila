package lila.puzzle

import scala.concurrent.duration._
import play.api.libs.json.{ JsValue, Json }
import lila.db.dsl._
import lila.user.User
import Puzzle.{ BSONFields => F }

import scala.concurrent.{ Await, Future }

final class PuzzleApi(
    puzzleColl: Coll,
    puzzleMigrationColl: Coll,
    roundColl: Coll,
    voteColl: Coll,
    headColl: Coll,
    puzzleIdMin: PuzzleId,
    asyncCache: lila.memo.AsyncCache.Builder,
    apiToken: String
) {

  import Puzzle.puzzleBSONHandler

  object puzzle {

    // Read
    def find(id: PuzzleId): Fu[Option[Puzzle]] = {
      newFind(id) // Call shadow read method
      puzzleColl.find($doc(F.id -> id)).uno[Puzzle]
    }

    // Read
    def findMany(ids: List[PuzzleId]): Fu[List[Option[Puzzle]]] = {
      newFindMany(ids) // Call shadow read method
      puzzleColl.optionsByOrderedIds[Puzzle, PuzzleId](ids)(_.id)
    }

    // Read
    def findAll(): Fu[List[Puzzle]] = {
      findAllNew() // Call shadow read method
      puzzleColl.find($empty).list[Puzzle]()
    }

    // Read
    def latest(nb: Int): Fu[List[Puzzle]] = {
      newLatest(nb) // Call shadow read method
      puzzleColl.find($empty)
        .sort($doc(F.date -> -1))
        .cursor[Puzzle]()
        .gather[List](nb)
    }

    // Shadow read
    def newFind(id: PuzzleId): Fu[Option[Puzzle]] =
      puzzleMigrationColl.find($doc(F.id -> id)).uno[Puzzle]

    // Shadow read
    def newFindMany(ids: List[PuzzleId]): Fu[List[Option[Puzzle]]] =
      puzzleMigrationColl.optionsByOrderedIds[Puzzle, PuzzleId](ids)(_.id)

    // Shadow read
    def findAllNew(): Fu[List[Puzzle]] =
      puzzleMigrationColl.find($empty).list[Puzzle]()

    // Shadow read
    def newLatest(nb: Int): Fu[List[Puzzle]] =
      puzzleMigrationColl.find($empty)
        .sort($doc(F.date -> -1))
        .cursor[Puzzle]()
        .gather[List](nb)

    val cachedLastId = asyncCache.single(
      name = "puzzle.lastId",
      f = lila.db.Util findNextId puzzleColl map (_ - 1),
      expireAfter = _.ExpireAfterWrite(1 day)
    )

    def importOne(json: JsValue, token: String): Fu[PuzzleId] =
      if (token != apiToken) fufail("Invalid API token")
      else {
        import Generated.generatedJSONRead
        insertPuzzle(json.as[Generated])
      }

    //For Generated
    def newInsertPuzzle(generated: Generated): Fu[PuzzleId] = {
      lila.db.Util findNextId puzzleMigrationColl flatMap { id =>
        val p = generated toPuzzle id
        val fenStart = p.fen.split(' ').take(2).mkString(" ")
        puzzleMigrationColl.exists($doc(
          F.id -> $gte(puzzleIdMin),
          F.fen.$regex(fenStart.replace("/", "\\/"), "")
        )) flatMap {
          case false => puzzleMigrationColl insert p inject id
          case _ => fufail(s"Duplicate puzzle $fenStart")
        }
      }
    }

    //For Puzzle
    def newInsertPuzzle(puzzle: Puzzle): Fu[PuzzleId] = {
      lila.db.Util findNextId puzzleMigrationColl flatMap { id =>
        val fenStart = puzzle.fen.split(' ').take(2).mkString(" ")
        puzzleMigrationColl.exists($doc(
          F.id -> $gte(puzzleIdMin),
          F.fen.$regex(fenStart.replace("/", "\\/"), "")
        )) flatMap {
          case false => puzzleMigrationColl insert puzzle inject id
          case _ => fufail(s"Duplicate puzzle $fenStart")
        }
      }
    }

    // Write method
    def insertPuzzle(generated: Generated): Fu[PuzzleId] = {
      lila.db.Util findNextId puzzleColl flatMap { id =>
        val p = generated toPuzzle id
        val fenStart = p.fen.split(' ').take(2).mkString(" ")
        puzzleColl.exists($doc(
          F.id -> $gte(puzzleIdMin),
          F.fen.$regex(fenStart.replace("/", "\\/"), "")
        )) flatMap {
          case false => puzzleColl insert p inject id
          case _ => fufail(s"Duplicate puzzle $fenStart")
        }
      }
      // Call Shadow write method
      newInsertPuzzle(generated)
    }

    def insertPuzzle(puzzle: Puzzle): Fu[PuzzleId] = {
      lila.db.Util findNextId puzzleColl flatMap { id =>
        val fenStart = puzzle.fen.split(' ').take(2).mkString(" ")
        puzzleColl.exists($doc(
          F.id -> $gte(puzzleIdMin),
          F.fen.$regex(fenStart.replace("/", "\\/"), "")
        )) flatMap {
          case false => puzzleColl insert puzzle inject id
          case _ => fufail(s"Duplicate puzzle $fenStart")
        }
      }
      // Call Shadow write method
      newInsertPuzzle(puzzle)
    }

    //    def shadowWriteConsistencyChecker(puzzle: Puzzle): Int = {
    //      var inconsistency = 0
    //
    //      val oldData = Await.result(fetchAll(), Duration.create(5, "seconds"))
    //      val oldDataList = oldData.flatten
    //
    //      val newData = Await.result(fetchAllNew(), Duration.create(5, "seconds"))
    //      val newDataList = newData.flatten
    //
    //      if (oldDataList contains puzzle)
    //        if (!(newDataList contains puzzle))
    //          inconsistency = 1
    //
    //      inconsistency
    //    }
    //
    //    def consistencyChecker(): Unit = {
    //      //Track inconsistencies
    //      var inconsistencies = 0
    //
    //      //Get data from the old table
    //      val oldData = Await.result(fetchAll(), Duration.create(5, "seconds"))
    //      val oldDataList = oldData.flatten
    //      //Get data from the new table
    //      val newData = Await.result(fetchAllNew(), Duration.create(5, "seconds"))
    //      val newDataList = newData.flatten
    //      //For each puzzle data in the old data, check that it matches the new data
    //      //For every puzzle in old data,
    //      //check that that puzzle id exists in new table with the correct game id
    //      var a = 0
    //      for (a <- oldDataList.indices) {
    //        val item = oldDataList(a)
    //        if (!(newDataList contains item)) {
    //          newInsertPuzzle(item)
    //          inconsistencies += 1
    //        }
    //      }
    //    }

    //    def forklift(): Unit = {
    //      //get old data
    //      // assume moving the entire old db
    //      val oldData = Await.result(fetchAll(), Duration.create(5, "seconds"))
    //      val oldDataList = oldData.flatten
    //
    //      //move all the old data to the new database
    //      for (a <- oldDataList.indices) {
    //        newInsertPuzzle(oldDataList(a))
    //      }
    //
    //    }

    def fetchAll(): Future[List[Puzzle]] = for {
      oldData <- findAll()
    } yield oldData

    def fetchAllNew(): Future[List[Puzzle]] = for {
      newData <- findAllNew()
    } yield newData

    def export(nb: Int): Fu[List[Puzzle]] = List(true, false).map { mate =>
      puzzleColl.find($doc(F.mate -> mate))
        .sort($doc(F.voteRatio -> -1))
        .cursor[Puzzle]().gather[List](nb / 2)
    }.sequenceFu.map(_.flatten)

    def disable(id: PuzzleId): Funit =
      puzzleColl.update(
        $id(id),
        $doc("$set" -> $doc(F.vote -> AggregateVote.disable))
      ).void
  }

  object round {

    def add(a: Round) = roundColl insert a
  }

  object vote {

    def value(id: PuzzleId, user: User): Fu[Option[Boolean]] =
      voteColl.primitiveOne[Boolean]($id(Vote.makeId(id, user.id)), "v")

    def find(id: PuzzleId, user: User): Fu[Option[Vote]] = voteColl.byId[Vote](Vote.makeId(id, user.id))

    def update(id: PuzzleId, user: User, v1: Option[Vote], v: Boolean): Fu[(Puzzle, Vote)] = puzzle find id flatMap {
      case None => fufail(s"Can't vote for non existing puzzle ${id}")
      case Some(p1) =>
        val (p2, v2) = v1 match {
          case Some(from) => (
            (p1 withVote (_.change(from.value, v))),
            from.copy(v = v)
          )
          case None => (
            (p1 withVote (_ add v)),
            Vote(Vote.makeId(id, user.id), v)
          )
        }
        voteColl.update(
          $id(v2.id),
          $set("v" -> v),
          upsert = true
        ) zip
          puzzleColl.update(
            $id(p2.id),
            $set(F.vote -> p2.vote)
          ) map {
              case _ => p2 -> v2
            }
    }
  }

  object head {

    def find(user: User): Fu[Option[PuzzleHead]] = headColl.byId[PuzzleHead](user.id)

    def set(h: PuzzleHead) = headColl.update($id(h.id), h, upsert = true) void

    def addNew(user: User, puzzleId: PuzzleId) = set(PuzzleHead(user.id, puzzleId.some, puzzleId))

    def solved(user: User, id: PuzzleId) = head find user flatMap {
      case Some(PuzzleHead(_, Some(c), n)) if c == id && c > n => set {
        PuzzleHead(user.id, none, id)
      }
      case Some(PuzzleHead(_, Some(c), n)) if c == id => headColl update (
        $id(user.id),
        $unset(PuzzleHead.BSONFields.current)
      )
      case _ => fuccess(none)
    }
  }
}
