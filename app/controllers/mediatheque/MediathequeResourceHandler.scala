package controllers.mediatheque

import javax.inject.{Inject, Singleton}
import models.Movie

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

/**
  * This singleton keep and manages the movie's data
  */
@Singleton
class MediathequeResourceHandler @Inject()()(implicit ec: ExecutionContext) {

  val movies: ListBuffer[Movie] = ListBuffer(
    Movie("Le premier film", "PYF", 2019, Some("The first movie"), None, None, List("animation"), 5),
    Movie("The movie", "FRA", 2011, None, None, None, List("sf"), 7.6f),
    Movie("Seigneur du film", "NZL", 2001, Some("Lord of the movie"), Some("2001/12/19"), Some(
        """
          |In the Second Age of Middle-earth, the lords of Elves, Dwarves, and Men are given Films of Power...
        """.stripMargin), List("fantasy", "sf"), 8.8f),
    Movie("Retour vers le film", "FRA", 2019, None, None, None, List("sf"), 8)
  )

  /**
    * Return all saved movies
    */
  def list(): Future[ListBuffer[Movie]] = {
    Future {
      movies
    }
  }

  /**
    * Add a new movie to the list
    */
  def create(movie: Movie): Unit = {
    movies += movie
  }
}
