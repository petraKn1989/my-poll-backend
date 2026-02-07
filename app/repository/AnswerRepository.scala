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
  val answersQuery = answers.filter(_.pollId === pollId).result
  val questionsQuery = questions.result
  val optionsQuery = options.result

  for {
    answerRows <- db.run(answersQuery)
    questionRows <- db.run(questionsQuery)
    optionRows <- db.run(optionsQuery)
  } yield {
    // Map questionId -> Question
    val questionsMap = questionRows.map(q => q.id -> q).toMap
    // Map optionId -> Option
    val optionsMap = optionRows.map(o => o.id -> o).toMap

    answerRows
      .groupBy(_.submissionId)
      .map { case (submissionId, rowsForSubmission) =>
        val note = rowsForSubmission.flatMap(_.submissionNote).headOption

        val details = rowsForSubmission.sortBy(_.createdAt).map { a =>
          val qText = questionsMap(a.questionId).text
          val oText = optionsMap(a.optionId).text
          AnswerDetail(qText, oText)
        }

        val submissionCreatedAt = rowsForSubmission.map(_.createdAt).min

        SubmissionSummary(submissionId, note, details, submissionCreatedAt)
      }
      .toSeq
      .sortBy(_.createdAt)
  }
}

}
