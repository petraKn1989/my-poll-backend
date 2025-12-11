package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import models._
import models.JsonFormats._
import repository.PollRepository

@Singleton
class PollController @Inject()(
    cc: ControllerComponents,
    pollRepo: PollRepository
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  def createPoll = Action(parse.json).async { request =>
    request.body.validate[Poll].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      poll => {
        val pollWithTime = poll.copy(createdAt = LocalDateTime.now())
        pollRepo.insertPoll(pollWithTime).map { id =>
          Created(Json.obj("status" -> "ok", "id" -> id))
        }
      }
    )
  }

  def getPoll(id: Long) = Action.async {
    pollRepo.getPollWithQuestionsAndOptions(id).map {
      case Some(poll) =>
        // Formátování createdAt
        val formattedPoll = poll.copy(createdAt = poll.createdAt)
        Ok(Json.toJson(formattedPoll))
      case None => NotFound(Json.obj("error" -> s"Poll with id $id not found"))
    }
  }
}
