package models

import play.api.libs.json._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object JsonFormats {

  // Formát pro LocalDateTime (musí být definován jako první!)
  implicit val localDateTimeFormat: Format[LocalDateTime] = new Format[LocalDateTime] {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    def writes(dt: LocalDateTime): JsValue = JsString(dt.format(formatter))
    def reads(json: JsValue): JsResult[LocalDateTime] = json match {
      case JsString(str) => JsSuccess(LocalDateTime.parse(str, formatter))
      case _ => JsError("Expected ISO_LOCAL_DATE_TIME formatted string")
    }
  }

  // ==================== GET / frontend ====================
  implicit val optionJsonFormat: OFormat[OptionJson] = Json.format[OptionJson]
  implicit val questionJsonFormat: OFormat[QuestionJson] = Json.format[QuestionJson]
  implicit val pollJsonFormat: OFormat[PollJson] = Json.format[PollJson]

  // ==================== POST / insert ====================
  implicit val questionFormat: OFormat[Question] = Json.format[Question]
  implicit val pollFormat: OFormat[Poll] = Json.format[Poll]

  // ==================== PATCH ====================
implicit val pollStatusPatchFormat: OFormat[PollStatusPatch] =
  Json.format[PollStatusPatch]

  // ==================== DTO ====================
  implicit val answerDtoFormat: OFormat[AnswerDto] = Json.format[AnswerDto]
  implicit val submitAnswersFormat: OFormat[SubmitAnswers] = Json.format[SubmitAnswers]
}
