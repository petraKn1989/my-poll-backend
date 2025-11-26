package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import models._
import repository.PollRepository
import models.JsonFormats._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PollController @Inject()(
    cc: ControllerComponents,
    pollRepo: PollRepository
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

 def createPoll = Action(parse.json).async { request =>
  request.body.validate[Poll].fold(
    errors => Future.successful(BadRequest(JsError.toJson(errors))),
    poll => {
      pollRepo.insertPoll(poll).map { id =>  // <- zachytíme ID
        Created(Json.obj(
          "status" -> "ok", // pokud chceš zachovat textovou informaci
          "id" -> id         // skutečné ID z DB
        ))
      }
    }
  )
}


def getPoll(id: Long) = Action.async {
  pollRepo.getPollWithQuestionsAndOptions(id).map {
    case Some(poll) => Ok(Json.toJson(poll))
    case None => NotFound(Json.obj("error" -> s"Poll with id $id not found"))
  }
}


}
