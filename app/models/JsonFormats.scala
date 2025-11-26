package models

import play.api.libs.json._

object JsonFormats {
  // GET / frontend
  implicit val optionJsonFormat: OFormat[OptionJson] = Json.format[OptionJson]
  implicit val questionJsonFormat: OFormat[QuestionJson] = Json.format[QuestionJson]
  implicit val pollJsonFormat: OFormat[PollJson] = Json.format[PollJson]

  // POST / insert
  implicit val questionFormat: OFormat[Question] = Json.format[Question]
  implicit val pollFormat: OFormat[Poll] = Json.format[Poll]

  // DTO
  implicit val answerDtoFormat: OFormat[AnswerDto] = Json.format[AnswerDto]
  implicit val submitAnswersFormat: OFormat[SubmitAnswers] = Json.format[SubmitAnswers]
}
