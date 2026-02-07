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
  private val questions = TableQuery[QuestionsTable] 
  private val options = TableQuery[OptionsTable]      

  /** Vloží jeden řádek odpovědi */
  def insertAnswer(answer: AnswerRow): Future[Long] =
    db.run((answers returning answers.map(_.id)) += answer)

  /** Vrátí všechny odpovědi pro daný průzkum */
  def getAnswersForPoll(pollId: Long): Future[Seq[AnswerRow]] =
    db.run(answers.filter(_.pollId === pollId).result)

  /** Vloží uživatele (volitelné) */
  def insertUser(user: UserRow): Future[Long] =
    db.run((users returning users.map(_.id)) += user)

    def getSubmissionsForPoll(pollId: Long): Future[Seq[SubmissionSummary]] = {
  val query = for {
    a <- answers if a.pollId === pollId
    q <- questions if q.id === a.questionId
    o <- options if o.id === a.optionId
  } yield (a.submissionId, a.submissionNote, q.text, o.text)

  db.run(query.result).map { rows =>
    rows.groupBy(_._1).map { case (submissionId, rowsForSubmission) =>
   
      val note = rowsForSubmission.flatMap(_._2).headOption

      val details = rowsForSubmission.map { case (_, _, qText, oText) =>
        AnswerDetail(qText, oText)
      }
      SubmissionSummary(submissionId, note, details)
    }.toSeq.sortBy(_.submissionId)
  }
}

}
