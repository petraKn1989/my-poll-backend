package repository

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._
import models._
import repository.Mappings._   // <- důležité, aby Slick věděl, jak mapovat LocalDateTime

@Singleton
class AnswerRepository @Inject()(implicit ec: ExecutionContext) {

  private val db = Database.forConfig("slick.dbs.default.db")
  private val answers = TableQuery[AnswersTable]
  private val users = TableQuery[UsersTable]

  /** Vloží jeden řádek odpovědi */
  def insertAnswer(answer: AnswerRow): Future[Long] =
    db.run((answers returning answers.map(_.id)) += answer)

  /** Vrátí všechny odpovědi pro daný průzkum */
  def getAnswersForPoll(pollId: Long): Future[Seq[AnswerRow]] =
    db.run(answers.filter(_.pollId === pollId).result)

  /** Vloží uživatele (volitelné) */
  def insertUser(user: UserRow): Future[Long] =
    db.run((users returning users.map(_.id)) += user)
}
