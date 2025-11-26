package models

import slick.jdbc.SQLiteProfile.api._

// ==================== Poll / Question / Option ====================

case class PollRow(id: Long = 0, createdAt: String)
class PollsTable(tag: Tag) extends Table[PollRow](tag, "polls") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def createdAt = column[String]("created_at")
  def * = (id, createdAt) <> (PollRow.tupled, PollRow.unapply)
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

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class AnswerRow(
  id: Long = 0,
  pollId: Long,
  questionId: Long,
  optionId: Long,
  userId: Option[Long] = None,
  createdAt: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
  submissionId: String
)



class AnswersTable(tag: Tag) extends Table[AnswerRow](tag, "answers") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def pollId = column[Long]("poll_id")
  def questionId = column[Long]("question_id")
  def optionId = column[Long]("option_id")
  def userId = column[Option[Long]]("user_id")
  def createdAt = column[String]("created_at") // zůstává NOT NULL
  def submissionId = column[String]("submission_id")
  def * = (id, pollId, questionId, optionId, userId, createdAt, submissionId) <> (AnswerRow.tupled, AnswerRow.unapply)

}



