package repository

import slick.jdbc.SQLiteProfile.api._
import models._

// ----- POLLS TABLE -----

class PollsTable(tag: Tag) extends Table[PollRow](tag, "polls") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def createdAt = column[String]("created_at")

  def * = (id, createdAt) <> (PollRow.tupled, PollRow.unapply)
}

// ----- QUESTIONS TABLE -----

class QuestionsTable(tag: Tag) extends Table[QuestionRow](tag, "questions") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def pollId = column[Long]("poll_id")
  def text = column[String]("text")
  def allowMultiple = column[Boolean]("allow_multiple")

  def * = (id, pollId, text, allowMultiple) <> (QuestionRow.tupled, QuestionRow.unapply)
}

// ----- OPTIONS TABLE -----

class OptionsTable(tag: Tag) extends Table[OptionRow](tag, "options") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def questionId = column[Long]("question_id")
  def text = column[String]("text")

  def * = (id, questionId, text) <> (OptionRow.tupled, OptionRow.unapply)
}
