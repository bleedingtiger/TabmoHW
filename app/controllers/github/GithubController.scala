package controllers.github

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc._
import utils.DateUtil

import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's API for Github.
  */
@Singleton
class GithubController @Inject()(cc: ControllerComponents, ws: WSClient, ec: ExecutionContext, config: Configuration) extends AbstractController(cc) {

  implicit val execCont: ExecutionContext = ec

  case class Author(name: String, email: String)
  case class Commiter(author: Author, commits: Int)

  implicit val commiterWrites: Writes[Commiter] = (commiter: Commiter) => Json.obj(
    "name" -> commiter.author.name,
    "email" -> commiter.author.email,
    "commits" -> commiter.commits
  )


  def getTopCommiters(repoOwner: String, repoName: String): Action[AnyContent] = Action.async {
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
    val futureResponse: Future[WSResponse] = ws.url(url).addHttpHeaders(("Authorization", "Bearer " + config.get[String]("token.githubGraphQL"))).post(data)
    futureResponse.map(rep => {
      val nodes = (rep.json \ "data" \ "repository" \ "defaultBranchRef" \ "target" \ "history" \ "edges") \\ "node"

      val authors = nodes.map(author => Author((author \ "author" \ "name").as[String], (author \ "author" \"email").as[String]))

      // Count commiters by grouping them, then sort them and take the first 10 to finally construct a Commiter
      val topCommiters = authors.groupBy(a => a).mapValues(_.size).toList.sortBy(_._2).reverse.take(10).map(a => Commiter(a._1, a._2))

      Ok(Json.toJson(topCommiters))
    })
  }


  def getTopLanguages(userName: String): Action[AnyContent] = Action.async {
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
    val futureRepoNames: Future[WSResponse] = ws.url(url).addHttpHeaders(("Authorization", "Bearer " + config.get[String]("token.githubGraphQL"))).post(data)

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
    } yield languages*/


    /*val futureResult: Future[Result] = futureList.map {
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




  def getIssues(repoOwner: String, repoName: String): Action[AnyContent] = Action.async {
    val dayHistorySize = 30
    val issuesMinDate = DateUtil.addDaysToDate(DateUtil.currentDate(), -dayHistorySize)

    val url = "https://api.github.com/graphql"
    val data = Json.obj(
      "query" -> {
        s"""query {
          search (first: 100, type:ISSUE, query: "repo:$repoOwner/$repoName created:>$issuesMinDate") {
            nodes {
              ... on Issue {
                createdAt
              }
            }
          }
        }"""
      }
    )
    val futureRepoIssues: Future[WSResponse] = ws.url(url).addHttpHeaders(("Authorization", "Bearer " + config.get[String]("token.githubGraphQL"))).post(data)

    val futureSortedIssuesDate = futureRepoIssues.map {
      res => {
        // Navigate GraphQL date structure to extract a map of dates
        val nodes = (res.json \ "data" \ "search" \ "nodes") \\ "createdAt"
        val issueDates = nodes.map(repo => repo.as[String]).map(d => DateUtil.convertGithubDate(d))
        val issuesPerDay = issueDates.groupBy(a => a).mapValues(_.size)
        // Fill the map holes with a date and a 0 for the issues number, then sort the map by date
        val filledIssuesDates = DateUtil.fillDatesMapWithZeros(issuesPerDay, issuesMinDate, dayHistorySize)
        val sortedIssuesDates = ListMap(filledIssuesDates.toSeq.sortBy(_._1):_*)
        sortedIssuesDates
      }
    }
    futureSortedIssuesDate.map(res => Ok(Json.toJson(res)))
  }

}
