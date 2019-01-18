package controllers.mediatheque

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's API for the Mediatheque.
  */
@Singleton
class MediathequeController @Inject()(cc: ControllerComponents,
                                      ws: WSClient,
                                      ec: ExecutionContext,
                                      mediathequeResHandler: MediathequeResourceHandler) extends AbstractController(cc) {

  implicit val execCont: ExecutionContext = ec

  def list(): Action[AnyContent] = Action.async {
    val listFilms = mediathequeResHandler.list()
    listFilms.map{
      res => {
        Ok(Json.toJson(res))
      }
    }
  }

  def create(): Action[AnyContent] = ???

  def find(genre: String): Action[AnyContent] = ???

  def group(): Action[AnyContent] = ???
}
