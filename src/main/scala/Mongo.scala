package lila.ws

import org.joda.time.DateTime
import reactivemongo.bson._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{ Cursor, DefaultDB, MongoConnection, MongoDriver }
import scala.concurrent.{ ExecutionContext, Future }

final class Mongo(implicit executionContext: ExecutionContext) {

  private val uri = Configuration.mongoUri

  private val driver = MongoDriver()
  private val parsedUri = MongoConnection.parseURI(uri)
  private val connection = Future.fromTry(parsedUri.flatMap(driver.connection(_, true)))

  private def db: Future[DefaultDB] = connection.flatMap(_.database("lichess"))
  private def securityColl = db.map(_.collection("security"))
  private def userColl = db.map(_.collection("user4"))

  def security[A](f: BSONCollection => Future[A]): Future[A] = securityColl flatMap f
  def user[A](f: BSONCollection => Future[A]): Future[A] = userColl flatMap f
}

object Mongo {

  implicit val BSONDateTimeHandler: BSONHandler[BSONDateTime, DateTime] =
    new BSONHandler[BSONDateTime, DateTime] {
      def read(time: BSONDateTime) = new DateTime(time.value)
      def write(jdtime: DateTime) = BSONDateTime(jdtime.getMillis)
    }
}