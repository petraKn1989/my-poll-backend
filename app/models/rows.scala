package models

import slick.jdbc.PostgresProfile.api._
import java.time.LocalDateTime
import repository.Mappings.localDateTimeColumnType



// ==================== Poll / Question / Option ====================

case class PollRow(
  id: Long = 0,
  createdAt: LocalDateTime,
  title: Option[String] = None,       // nový sloupec, nullable
  showResults: Boolean = false,        // nový sloupec, default false
  status: String = "active", // default
  slug: String = "temp"  // nový sloupec, default "temp"
)

class PollsTable(tag: Tag) extends Table[PollRow](tag, "polls") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def createdAt = column[LocalDateTime]("created_at")
  def title = column[Option[String]]("title")                   // nový sloupec
  def showResults = column[Boolean]("show_results", O.Default(false)) // nový sloupec s defaultem
  def status = column[String]("status") // VARCHAR
  def slug = column[String]("slug") // nový sloupec

  // mapování do PollRow
  def * = (id, createdAt, title, showResults, status, slug) <> (PollRow.tupled, PollRow.unapply)
}



case class QuestionRow(id: Long = 0, pollId: Long, text: String, allowMultiple: Boolean)
class QuestionsTable(tag: Tag) extends Table[QuestionRow](tag, "questions") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def pollId = column[Long]("poll_id")
  def text = column[String]("text")
  def allowMultiple = column[Boolean]("allow_multiple")
  def * = (id, pollId, text, allowMultiple) <> (QuestionRow.tupled, QuestionRow.unapply)
}

case class OptionRow(id: Long = 0, questionId: Long, text: String)
class OptionsTable(tag: Tag) extends Table[OptionRow](tag, "options") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def questionId = column[Long]("question_id")
  def text = column[String]("text")
  def * = (id, questionId, text) <> (OptionRow.tupled, OptionRow.unapply)
}

// ==================== Users / Answers ====================

case class UserRow(id: Long = 0, name: Option[String] = None, email: Option[String] = None)
class UsersTable(tag: Tag) extends Table[UserRow](tag, "users") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[Option[String]]("name")
  def email = column[Option[String]]("email")
  def * = (id, name, email) <> (UserRow.tupled, UserRow.unapply)
}

case class AnswerRow(
  id: Long = 0,
  pollId: Long,
  questionId: Long,
  optionId: Long,
  userId: Option[Long] = None,
  
  createdAt: LocalDateTime = LocalDateTime.now(),
  submissionId: String
)

class AnswersTable(tag: Tag) extends Table[AnswerRow](tag, "answers") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def pollId = column[Long]("poll_id")
  def questionId = column[Long]("question_id")
  def optionId = column[Long]("option_id")
  def userId = column[Option[Long]]("user_id")
  def createdAt = column[LocalDateTime]("created_at")
  def submissionId = column[String]("submission_id")

  def * = (id, pollId, questionId, optionId, userId, createdAt, submissionId) <> (AnswerRow.tupled, AnswerRow.unapply)
}
