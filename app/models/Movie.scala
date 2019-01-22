package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import utils.DateUtil

case class Movie(
                  title: String,
                  country: String,
                  year: Int,
                  original_title: Option[String] = None,
                  french_release: Option[String] = None,
                  synopsis: Option[String] = None,
                  genre: List[String],
                  ranking: Float
                )

object Movie {
  implicit val implicitWrites: Writes[Movie] = (film: Movie) => Json.obj(
    "title" -> film.title,
    "country" -> film.country,
    "year" -> film.year,
    "original_title" -> {
      film.original_title match {
        case Some(d) => d
        case None => ""
      }
    },
    "french_release" -> {
      film.french_release match {
        case Some(d) => d
        case None => ""
      }
    },
    "synopsis" -> {
      film.synopsis match {
        case Some(d) => d
        case None => ""
      }
    },
    "genre" -> Json.toJson(film.genre),
    "ranking" -> film.ranking // Precision error here due to Float, cast to string can solve it if required
  )

  implicit val implicitReads: Reads[Movie] = (
    (JsPath \ 'title).read[String](minLength[String](1) keepAnd maxLength[String](250)) and
      (JsPath \ 'country).read[String].filter(JsonValidationError("must valid the format ISO 3166-1 alpha-3"))(c => c.matches("""[A-Z]{3}""")) and
      (JsPath \ 'year).read[Int](min(1800) keepAnd max(9999)) and
      (JsPath \ 'original_title).readNullable[String](minLength[String](1) keepAnd maxLength[String](250)) and
      (JsPath \ 'french_release).readNullable[String].filter(JsonValidationError("must valid the format yyyy/MM/dd")) {
        case Some(a) => a.matches(DateUtil.movieDateRegex)
        case None => true
      } and
      (JsPath \ 'synopsis).readNullable[String](maxLength[String](9999)) and
      (JsPath \ 'genre).read[List[String]]
        .filter(JsonValidationError("a movie must have at least 1 genre"))(c => c.nonEmpty)
        .filter(JsonValidationError("every genre must be between 1 and 50 char. max."))(c => {
          c.map(s => s.length > 0 && s.length <= 50).reduce((a, b) => a && b)
        }) and
      (JsPath \ 'ranking).read[Float](min(0.0f) keepAnd max(10.0f)).filter(JsonValidationError("must be incremented by 0.1"))
      (r => (r * 1000).round % 100 == 0) // Allow some imprecision to counter float/double lack of precision
    ) (Movie.apply _)
}

