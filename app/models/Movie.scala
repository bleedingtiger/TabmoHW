package models

import play.api.libs.json.{Json, Writes}

case class Movie(
                 title: String,
                 country: String,
                 year: Short,
                 original_title: Option[String] = None,
                 french_release: Option[String] = None,
                 synopsis: Option[String] = None,
                 genre: List[String],
                 ranking: Short
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
    }},
    "french_release" -> {
      film.french_release match {
      case Some(d) => d
      case None => ""
    }},
    "synopsis" -> {
      film.synopsis match {
      case Some(d) => d
      case None => ""
    }},
    "genre" -> Json.toJson(film.genre),
    "ranking" -> film.ranking,
  )
}