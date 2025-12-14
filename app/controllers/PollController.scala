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
      // Nastavení aktuálního času
      val now = LocalDateTime.now()
      val pollWithCreatedAt = poll.copy(createdAt = now)

      // Zavolání repository – necháme ji doplnit slug a status
     pollRepo.insertPoll(pollWithCreatedAt).flatMap { id =>
  pollRepo.getPollWithQuestionsAndOptions(id).map {
    case Some(pollFromDb) =>
      Created(Json.obj(
        "status" -> "ok",
        "id" -> id,
        "title" -> poll.title,
        "showResults" -> poll.showResults,
        "slug" -> pollFromDb.slug
      ))
    case None =>
      InternalServerError("Poll was not found after insert")
  }
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

  def getPollBySlug(slug: String) = Action.async {
  pollRepo.getPollWithQuestionsAndOptionsBySlug(slug).map {
    case Some(poll) =>
      // Formátování createdAt zůstává stejné
      val formattedPoll = poll.copy(createdAt = poll.createdAt)
      Ok(Json.toJson(formattedPoll))
    case None =>
      NotFound(Json.obj("error" -> s"Poll with slug $slug not found"))
  }
}


  def deletePoll(id: Long) = Action.async {
  pollRepo.deletePoll(id).map {
    case true  => Ok(Json.obj("status" -> "ok", "message" -> s"Poll $id deleted successfully"))
    case false => NotFound(Json.obj("status" -> "error", "message" -> s"Poll $id not found"))
  }
}

private def generateSlug(title: Option[String], uniqueId: Long): String = {
  val base = title.getOrElse("anketa")
    .toLowerCase
    .replaceAll("[^a-z0-9]+", "-")
    .take(50) // max délka slug
  s"$base-$uniqueId"
}


def patchStatus(id: Long) = Action(parse.json).async { request =>
  request.body.validate[PollStatusPatch] match {

    case JsSuccess(patch, _) =>
      val allowedStatuses = Set(
        "active",
        "paused",
        "deleted",
        "finished_hidden",
        "finished_published"
      )

      if (!allowedStatuses.contains(patch.status))
        Future.successful(BadRequest("Unknown status"))
      else
        pollRepo.updateStatus(id, patch.status).map {
          case 0 => NotFound("Poll not found")
          case _ => Ok(Json.obj("status" -> patch.status))
        }

    case JsError(_) =>
      Future.successful(BadRequest("Invalid status JSON"))
  }
}

def patchStatusBySlug(slug: String) = Action.async(parse.json) { request =>
  request.body.validate[PollStatusPatch] match {

    case JsSuccess(patch, _) =>
      val allowedStatuses = Set(
        "active",
        "paused",
        "deleted",
        "finished_hidden",
        "finished_published"
      )

      if (!allowedStatuses.contains(patch.status))
        Future.successful(BadRequest("Unknown status"))
      else
        pollRepo.getPollWithQuestionsAndOptionsBySlug(slug).flatMap {
          case Some(poll) =>
            pollRepo.updateStatus(poll.id, patch.status).map { _ =>
              Ok(Json.obj("status" -> patch.status))
            }
          case None =>
            Future.successful(NotFound(Json.obj("error" -> s"Poll with slug $slug not found")))
        }

    case JsError(_) =>
      Future.successful(BadRequest("Invalid status JSON"))
  }
}





}
