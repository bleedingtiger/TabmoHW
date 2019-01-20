package models

import play.api.libs.json._

case class Commiter(author: (String, String), commits: Int)

object Commiter {
  implicit val commiterWrites: Writes[Commiter] = (commiter: Commiter) => Json.obj(
    "name" -> commiter.author._1,
    "email" -> commiter.author._2,
    "commits" -> commiter.commits
  )
}