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
  * application's API for the Epic 1.
  */
@Singleton
class MediathequeController @Inject()(cc: ControllerComponents,
                                      ws: WSClient,
                                      ec: ExecutionContext,
                                      mediathequeResHandler: MediathequeResourceHandler) extends AbstractController(cc) {

  implicit val execCont: ExecutionContext = ec

  /**
    * US 1-1 : Tries to create a new movie be reading POST data as JSON
    * @return Message of success or message describing the error
    */
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

  /**
    * US 1-2 : Lists all movies of a specific genre if an argument is passed, otherwise lists all movies
    * @param genre String representing the genre to filter
    * @return
    */
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

  /**
    * US 1-3 : Group movies by year of production and count them
    * @return a list of years with the number of movies associated, as JSON data
    */
  def groupByYear(): Action[AnyContent] = Action.async {
    val movieList = mediathequeResHandler.list()
    movieList.map {
      m => {
        val mList = ListMap(m.toList.groupBy(m => m.year).mapValues(_.size).toSeq.sortBy(-_._1): _*)
        Ok(Json.toJson(mList))
      }
    }
  }
}
