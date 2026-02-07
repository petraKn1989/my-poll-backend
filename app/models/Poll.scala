package models

import play.api.libs.json._
import java.time.LocalDateTime

// ==================== DTO pro frontend ====================

case class AnswerDto(questionId: Long, selectedOptionIds: Seq[Long])
case class SubmitAnswers(pollId: Long, userId: Option[Long], answers: Seq[AnswerDto], note: Option[String] = None)

case class OptionJson(id: Long, text: String, votes: Int)
case class QuestionJson(id: Long, text: String, allowMultiple: Boolean, options: Seq[OptionJson], totalVotes: Int)

case class PollJson(id: Long, title: Option[String], showResults: Boolean, createdAt: String, questions: Seq[QuestionJson], totalVotes: Int,
status: String, slug: String, allowVote: Option[Boolean] = Some(false))

case class Question(text: String, allowMultiple: Boolean, options: List[String])
case class Poll(
  createdAt: LocalDateTime, 
  questions: List[Question], 
  title: Option[String] = None, 
  showResults: Boolean,  
  status: Option[String],   // teď optional
  slug: Option[String]      // teď optional
)
case class PollStatusPatch(status: String)

case class AnswerDetail(
  questionText: String,
  optionText: String
)


case class SubmissionSummary(
  submissionId: String,
  note: Option[String],
  answers: Seq[AnswerDetail],
  createdAt: LocalDateTime

)

