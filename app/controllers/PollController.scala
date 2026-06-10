package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDateTime
import models._
import models.JsonFormats._
import repository.PollRepository

@Singleton
class PollController @Inject()(
    cc: ControllerComponents,
    pollRepo: PollRepository
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

 def createPoll = Action(parse.json).async { request =>
  val clientIp = request.remoteAddress
  request.body.validate[Poll].fold(
    errors => Future.successful(BadRequest(JsError.toJson(errors))),
    poll => {
      val pollWithCreatedAt = poll.copy(createdAt = LocalDateTime.now())
      for {
        id <- pollRepo.insertPoll(pollWithCreatedAt)
    
        pollFromDb <- pollRepo.getPollWithQuestionsAndOptions(id, "")
      } yield pollFromDb match {
        case Some(p) => Created(Json.toJson(p))
        case None => InternalServerError("Poll was not found after insert")
      }
    }
  )
}


  def getPoll(id: Long) = Action.async { request =>
    pollRepo.getPollWithQuestionsAndOptions(id, "").map {
    case Some(p) => Ok(Json.toJson(p))
    case None    => NotFound(Json.obj("error" -> s"Poll with id $id not found"))
  }
  }

  def getPollBySlug(slug: String) = Action.async { request =>
  // Vytáhneme deviceUuid přímo z URL dotazu (?deviceUuid=...)
  // Play framework nám vrátí Option[String] automaticky
  val deviceUuidFromUrl = request.getQueryString("deviceUuid")

  // Vyčistíme případný textový "null" z Angularu
  val cleanUuid = deviceUuidFromUrl.filter(id => id.nonEmpty && id != "null").getOrElse("")

  // Posíláme do repozitáře
  pollRepo.getPollWithQuestionsAndOptionsBySlug(slug, cleanUuid).map {
    case Some(pollJson) => Ok(Json.toJson(pollJson))
    case None           => NotFound(Json.obj("error" -> "Anketa nenalezena"))
  }
  }

def submitVote(pollId: Long) = Action(parse.json).async { request =>
  val clientIp = request.remoteAddress
  request.body.validate[SubmitAnswers].fold(
    errors => Future.successful(BadRequest(JsError.toJson(errors))),
    submitAnswers => {
      for {
     
        _ <- pollRepo.insertVote(pollId, clientIp, submitAnswers.deviceUuid)
        _ <- pollRepo.insertAnswers(pollId, submitAnswers.answers)
      } yield Ok(Json.obj("allowVote" -> true))
    }
  )
}


  def deletePoll(id: Long) = Action.async {
    pollRepo.deletePoll(id).map {
      case true  => Ok(Json.obj("status" -> "ok", "message" -> s"Poll $id deleted successfully"))
      case false => NotFound(Json.obj("status" -> "error", "message" -> s"Poll $id not found"))
    }
  }

  def patchStatus(id: Long) = Action(parse.json).async { request =>
    request.body.validate[PollStatusPatch] match {
      case JsSuccess(patch, _) =>
        val allowed = Set("active","paused","deleted","finished_hidden","finished_published")
        if (!allowed.contains(patch.status)) Future.successful(BadRequest("Unknown status"))
        else pollRepo.updateStatus(id, patch.status).map {
          case 0 => NotFound("Poll not found")
          case _ => Ok(Json.obj("status" -> patch.status))
        }
      case JsError(_) => Future.successful(BadRequest("Invalid status JSON"))
    }
  }

  def patchStatusBySlug(slug: String) = Action(parse.json).async { request =>
    request.body.validate[PollStatusPatch] match {
      case JsSuccess(patch, _) =>
        val allowed = Set("active","paused","deleted","finished_hidden","finished_published")
        if (!allowed.contains(patch.status)) Future.successful(BadRequest("Unknown status"))
        else pollRepo.getPollWithQuestionsAndOptionsBySlug(slug, "0.0.0.0").flatMap {
          case Some(poll) => pollRepo.updateStatus(poll.id, patch.status).map(_ => Ok(Json.obj("status" -> patch.status)))
          case None => Future.successful(NotFound(Json.obj("error" -> s"Poll with slug $slug not found")))
        }
      case JsError(_) => Future.successful(BadRequest("Invalid status JSON"))
    }
  }
}
