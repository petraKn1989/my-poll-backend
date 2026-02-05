package controllers

import javax.inject._
import play.api.mvc._
import play.api.mvc.AbstractController   // ⬅️ DŮLEŽITÉ
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import models._
import models.JsonFormats._
import repository.AnswerRepository
import repository.PollRepository
import java.time.LocalDateTime

@Singleton
class AnswerController @Inject()(
    cc: ControllerComponents,
    answerRepo: AnswerRepository,
    pollRepo: PollRepository
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

 def submitAnswers = Action(parse.json).async { request =>
  request.body.validate[SubmitAnswers].fold(
    errors => Future.successful(BadRequest(JsError.toJson(errors))),
    data => {
      val clientIp = request.headers
        .get("X-Forwarded-For")
        .getOrElse(request.remoteAddress)

      pollRepo.hasVoted(data.pollId, clientIp).flatMap { voted =>
        if (voted) {
          Future.successful(Ok(Json.obj("status" -> "ok", "allowVote" -> false)))
        } else {

          val submissionId = java.util.UUID.randomUUID().toString

          // vložení odpovědí + submissionNote jen u první odpovědi
          val actions = data.answers.flatMap { a =>
            a.selectedOptionIds.zipWithIndex.map { case (optionId, idx) =>
              answerRepo.insertAnswer(
                AnswerRow(
                  id = 0L,
                  pollId = data.pollId,
                  questionId = a.questionId,
                  optionId = optionId,
                  userId = data.userId,
                  createdAt = LocalDateTime.now(),
                  submissionId = submissionId,
                  submissionNote = if(idx == 0) data.note else None  // poznámka jen u první odpovědi
                )
              )
            }
          }

          for {
            _ <- Future.sequence(actions)
            _ <- pollRepo.insertVote(data.pollId, clientIp)
          } yield Ok(Json.obj("status" -> "ok", "allowVote" -> true))
        }
      }
    }
  )
}

def getSubmissions(pollId: Long) = Action.async {
  answerRepo.getSubmissionsForPoll(pollId).map { submissions =>
    Ok(Json.toJson(submissions))
  }
}

}
