package controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.mvc.ControllerComponents
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Reads._

import scala.concurrent.{ExecutionContext, Future}

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's API.
  */
@Singleton
class Api @Inject()(cc: ControllerComponents, ws: WSClient, ec: ExecutionContext) extends AbstractController(cc) {

  implicit val execCont: ExecutionContext = ec
  val githubGraphQLToken = "5e3126eee18c0ee160caff32b2c8e563b7a148d5"

  case class Author(name: String, email: String)
  case class Commiter(author: Author, commits: Int)

  implicit val commiterWrites = new Writes[Commiter] {
    def writes(commiter: Commiter) = Json.obj(
      "name" -> commiter.author.name,
      "email" -> commiter.author.email,
      "commits" -> commiter.commits
    )
  }


  def getTopCommiters(repoName: String, repoOwner: String) = Action.async {
    val url = "https://api.github.com/graphql"

    // TODO : More elegant way to represent the GraphQL query
    val data = Json.obj(
      "query" -> {
        "query {" +
            "repository(name: " + repoName + ", owner: " + repoOwner + ") {" +
              "defaultBranchRef {" +
                "target {" +
                  "... on Commit {" +
                    "history(first: 100) {" +
                      "edges {" +
                        "node {" +
                          "author {" +
                            "name \n" +
                            "email" +
                          "}" +
                        "}" +
                      "}" +
                    "}" +
                  "}" +
                "}" +
              "}" +
            "}" +
          "}"
      }
    )
    val futureResponse: Future[JsValue] = ws.url(url).addHttpHeaders(("Authorization", "Bearer " + githubGraphQLToken)).post(data).map {
      response => response.json
    }
    futureResponse.map(rep => {
      val nodes = (rep \ "data" \ "repository" \ "defaultBranchRef" \ "target" \ "history" \ "edges") \\ "node"

      val authors = nodes.map(author => new Author((author \ "author" \ "name").as[String], (author \ "author" \"email").as[String]))

      // Count commiters by grouping them, then sort them and take the first 10 to finally construct a Commiter
      val topCommiters = authors.groupBy(a => a).mapValues(_.size).toList.sortBy(_._2).reverse.take(10).map(a => Commiter(a._1, a._2))

      Ok(Json.toJson(topCommiters))
    })
  }

}
