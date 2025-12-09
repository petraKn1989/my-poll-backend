package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import models.JsonFormats._
import scala.concurrent.Future

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

     println("RAILWAY_HOST = " + sys.env.get("RAILWAY_HOST"))
  println("PORT = " + sys.env.get("PORT"))
def home() = Action {
   // Tady se vypíšou env proměnné při startu

  Ok("Server běží! Endpoint /home funguje.")
}

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }
}
