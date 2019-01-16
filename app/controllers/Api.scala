package controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc.ControllerComponents
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Reads._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's API.
  */
@Singleton
class Api @Inject()(cc: ControllerComponents, ws: WSClient, ec: ExecutionContext) extends AbstractController(cc) {

  implicit val execCont: ExecutionContext = ec
  val githubGraphQLToken = "f36b5d5274d4e9dc73d57d4cededd9d18b1a8d55"

  case class Author(name: String, email: String)
  case class Commiter(author: Author, commits: Int)

  implicit val commiterWrites = new Writes[Commiter] {
    def writes(commiter: Commiter) = Json.obj(
      "name" -> commiter.author.name,
      "email" -> commiter.author.email,
      "commits" -> commiter.commits
    )
  }


  def getTopCommiters(repoOwner: String, repoName: String) = Action.async {
    val url = "https://api.github.com/graphql"

    val data = Json.obj(
      "query" -> {
        s"""query {
            repository(name: $repoName, owner: $repoOwner) {
              defaultBranchRef {
                target {
                  ... on Commit {
                    history(first: 100) {
                      edges {
                        node {
                          author {
                            name \n
                            email
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }"""
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


  def getTopLanguages(userName: String) = Action.async {
    val url = "https://api.github.com/graphql"

    val data = Json.obj(
      "query" -> {
        s"""query {
          user(login: $userName) {
            repositories(first: 100, isFork: false) {
              nodes {
                name
              }
            }
          }
        }"""
      }
    )
    val futureRepoNames: Future[WSResponse] = ws.url(url).addHttpHeaders(("Authorization", "Bearer " + githubGraphQLToken)).post(data)

    val futureRepoNameList: Future[List[String]] = futureRepoNames.map {
      res => {
        val nodes = (res.json \ "data" \ "user" \ "repositories" \ "nodes") \\ "name"
        val repoNames = nodes.map(repo => repo.as[String])
        repoNames.toList
      }
    }
    /*val futureList: Future[List[Future[WSResponse]]] = futureRepoNameList.map {
      res => {
        res.map(repoName => {
          val urlBis = s"https://api.github.com/repos/$userName/$repoName/languages"
          ws.url(urlBis).get()
        })
      }
    }

    val futureFinal = futureList.map {
      res => {
        res.map(f => f.map(r => println(r.json)))
      }
    }*/


    /*val futureList: Future[List[WSResponse]] =
    for {
      repoName <- futureRepoNameList
      languages <- Future.sequence {
        val urlBis = s"https://api.github.com/repos/$userName/$repoName/languages"
        ws.url(urlBis).get()
      }
    } yield languages


    val futureResult: Future[Result] = futureList.map {
      res => {
        //println(res)
        Ok("test")
      }
    }
    futureResult*/

    Future { Ok("TODO") }



    /*val futureLanguages: Future[Future[WSResponse]] = futureRepoNameList.map {
      res => {
        val urlBis = s"https://api.github.com/repos/$userName/$res/languages"
        ws.url(urlBis).get()
      }
    }
    val flattenFuture = futureLanguages.flatten.map { res => println(res.json)}*/
  }
}
