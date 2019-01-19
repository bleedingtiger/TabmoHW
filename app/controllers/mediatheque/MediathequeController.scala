package controllers.mediatheque

import javax.inject.{Inject, Singleton}
import models.Movie
import play.api.libs.ws.WSClient
import play.api.libs.json._
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext

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

  def create(): Action[JsValue] = Action(parse.json) {
    request => {
      request.body.validate[Movie].map {
        m => {
          mediathequeResHandler.create(m)
          Ok("New movie " + m.title + " (" + m.year + ") successfully created !")
        }
      }.recoverTotal {
        e => BadRequest("Error: " + JsError.toJson(e))
      }
    }
  }

  def list(genre: Option[String]): Action[AnyContent] = Action.async {
    val movieList = mediathequeResHandler.list()
    movieList.map {
      m => {
        genre match {
          case Some(g) =>
            val mList = m.toList.filter(_.genre.contains(g)).sortBy(m => (-m.year, m.title))
            Ok(Json.toJson(mList))
          case None =>
            val mList = m.toList.sortBy(m => (-m.year, m.title))
            Ok(Json.toJson(mList))
        }
      }
    }
  }

  def groupByYear(): Action[AnyContent] = Action.async {
    val movieList = mediathequeResHandler.list()
    movieList.map {
      m => {
        val mList = ListMap(m.toList.groupBy(m => m.year).mapValues(_.size).toSeq.sortBy(-_._1):_*)
        Ok(Json.toJson(mList))
      }
    }
  }
}
