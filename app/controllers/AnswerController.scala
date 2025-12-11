package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import models._
import models.JsonFormats._
import repository.AnswerRepository
import java.time.LocalDateTime

@Singleton
class AnswerController @Inject()(
    cc: ControllerComponents,
    answerRepo: AnswerRepository
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  /** Endpoint pro odeslání odpovědí */
  def submitAnswers = Action(parse.json).async { request =>
    request.body.validate[SubmitAnswers].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      data => {
        val submissionId = java.util.UUID.randomUUID().toString

        val actions: Seq[Future[Long]] = data.answers.flatMap { a =>
          a.selectedOptionIds.map { optionId =>
            answerRepo.insertAnswer(
              AnswerRow(
                id = 0L,
                pollId = data.pollId,
                questionId = a.questionId,
                optionId = optionId,
                userId = data.userId,
                createdAt = LocalDateTime.now(), // LocalDateTime se mapuje přes Mappings.scala
                submissionId = submissionId
              )
            )
          }
        }

        Future.sequence(actions).map(_ => Ok(Json.obj("status" -> "ok")))
      }
    )
  }
}
