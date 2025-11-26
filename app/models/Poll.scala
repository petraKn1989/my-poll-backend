package models

import play.api.libs.json._

// ==================== DTO pro frontend ====================

case class AnswerDto(questionId: Long, selectedOptionIds: Seq[Long])
case class SubmitAnswers(pollId: Long, userId: Option[Long], answers: Seq[AnswerDto])

case class OptionJson(id: Long, text: String, votes: Int)
case class QuestionJson(id: Long, text: String, allowMultiple: Boolean, options: Seq[OptionJson], totalVotes: Int)
case class PollJson(id: Long, createdAt: String, questions: Seq[QuestionJson], totalVotes: Int)

case class Question(text: String, allowMultiple: Boolean, options: List[String])
case class Poll(createdAt: String, questions: List[Question])
