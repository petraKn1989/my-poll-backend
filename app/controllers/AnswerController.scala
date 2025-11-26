package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import models._
import models.JsonFormats._
import repository.AnswerRepository

@Singleton
class AnswerController @Inject()(
    cc: ControllerComponents,
    answerRepo: AnswerRepository
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // Endpoint pro odeslání odpovědí
def submitAnswers = Action(parse.json).async { request =>
  request.body.validate[SubmitAnswers].fold(
    errors => Future.successful(BadRequest(JsError.toJson(errors))),
    data => {
      // 1️⃣ Vygenerujeme nové submissionId pro celé hlasování
      val submissionId = java.util.UUID.randomUUID().toString

      // 2️⃣ Vytvoříme seznam AnswerRow pro každou vybranou možnost
      val actions: Seq[Future[Long]] = data.answers.flatMap { a =>
        a.selectedOptionIds.map { optionId =>
          answerRepo.insertAnswer(
            AnswerRow(
              id = 0L, // nebo default
              pollId = data.pollId,
              questionId = a.questionId,
              optionId = optionId,
              userId = data.userId,
              createdAt = java.time.Instant.now.toString,
              submissionId = submissionId // stejné pro všechny odpovědi hlasování
            )
          )
        }
      }

      Future.sequence(actions).map(_ => Ok(Json.obj("status" -> "ok")))
    }
  )
}

}
