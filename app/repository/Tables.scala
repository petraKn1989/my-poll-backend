package repository

import slick.jdbc.PostgresProfile.api._
import models._
import java.time.LocalDateTime
import repository.Mappings.localDateTimeColumnType  // import implicitního mapování

// ============================
// POLLS TABLE
// ============================
class PollsTable(tag: Tag) extends Table[PollRow](tag, "polls") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def createdAt = column[LocalDateTime]("created_at")

  def * = (id, createdAt) <> (PollRow.tupled, PollRow.unapply)
}

// ============================
// QUESTIONS TABLE
// ============================
class QuestionsTable(tag: Tag) extends Table[QuestionRow](tag, "questions") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def pollId = column[Long]("poll_id")
  def text = column[String]("text")
  def allowMultiple = column[Boolean]("allow_multiple")

  def * = (id, pollId, text, allowMultiple) <> (QuestionRow.tupled, QuestionRow.unapply)
}

// ============================
// OPTIONS TABLE
// ============================
class OptionsTable(tag: Tag) extends Table[OptionRow](tag, "options") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def questionId = column[Long]("question_id")
  def text = column[String]("text")

  def * = (id, questionId, text) <> (OptionRow.tupled, OptionRow.unapply)
}

// ============================
// USERS TABLE
// ============================
class UsersTable(tag: Tag) extends Table[UserRow](tag, "users") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[Option[String]]("name")
  def email = column[Option[String]]("email")

  def * = (id, name, email) <> (UserRow.tupled, UserRow.unapply)
}

// ============================
// ANSWERS TABLE
// ============================
class AnswersTable(tag: Tag) extends Table[AnswerRow](tag, "answers") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def pollId = column[Long]("poll_id")
  def questionId = column[Long]("question_id")
  def optionId = column[Long]("option_id")
  def userId = column[Option[Long]]("user_id")
  def createdAt = column[LocalDateTime]("created_at")  // LocalDateTime díky mappingu
  def submissionId = column[String]("submission_id")

  def * = (id, pollId, questionId, optionId, userId, createdAt, submissionId) <> (AnswerRow.tupled, AnswerRow.unapply)
}
