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
 val deviceUuidOpt = (request.body \ "deviceUuid").asOpt[String]
    .orElse(request.getQueryString("deviceUuid"))
  val cleanUuid = deviceUuidOpt.filter(id => id.nonEmpty && id != "null").getOrElse("")

  request.body.validate[SubmitAnswers].fold(
    errors => Future.successful(BadRequest(JsError.toJson(errors))),
    data => {
      val clientIp = request.headers
        .get("X-Forwarded-For")
        .getOrElse(request.remoteAddress)

      // 2. OPRAVENO: Posíláme cleanUuid, aby repozitář ověřil duplicitu podle UUID!
      pollRepo.hasVoted(data.pollId, cleanUuid).flatMap { voted =>
        if (voted) {
          Future.successful(Ok(Json.obj("status" -> "ok", "allowVote" -> false)))
        } else {

          val submissionId = java.util.UUID.randomUUID().toString

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
                  submissionNote = if(idx == 0) data.note else None
                )
              )
            }
          }

          for {
            _ <- Future.sequence(actions)
            // Tady ukládáme obojí (IP i UUID)
            _ <- pollRepo.insertVote(data.pollId, clientIp, cleanUuid)
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