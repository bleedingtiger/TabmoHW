package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

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
    "ranking" -> film.ranking,
  )

  def validCountry(country: String): Boolean = country.matches("""[A-Z]{3}""")

  implicit val implicitReads: Reads[Movie] = (
    (JsPath \ 'title).read[String](maxLength[String](250)) and
      (JsPath \ 'country).read[String](minLength[String](3) keepAnd maxLength[String](3)) and
      (JsPath \ 'year).read[Int](min(1800) keepAnd max(3000)) and
      (JsPath \ 'original_title).formatNullable[String] and
      (JsPath \ 'french_release).formatNullable[String] and
      (JsPath \ 'synopsis).formatNullable[String] and
      (JsPath \ 'genre).read[List[String]] and
      (JsPath \ 'ranking).read[Float]
    ) (Movie.apply _)
}