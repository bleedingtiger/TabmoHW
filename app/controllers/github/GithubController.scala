package controllers.github

import javax.inject.{Inject, Singleton}
import models.Commiter
import play.api.Configuration
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc._
import utils.DateUtil

import scala.collection.immutable.ListMap
import scala.collection.Map
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's API for Epic 2.
  */
@Singleton
class GithubController @Inject()(
                                  cc: ControllerComponents,
                                  ws: WSClient,
                                  ec: ExecutionContext,
                                  githubResourceHandler: GithubResourceHandler,
                                  config: Configuration) extends AbstractController(cc) {

  implicit val execCont: ExecutionContext = ec


  /**
    * US 2-1 : Return top 10 commiters of a specific Github project
    */
  def getTopCommiters(repoOwner: String, repoName: String): Action[AnyContent] = Action.async {

    val futureTopCommiters: Future[WSResponse] = githubResourceHandler.topCommiters(repoOwner, repoName)
    futureTopCommiters.map(rep => {
      val nodes = (rep.json \ "data" \ "repository" \ "defaultBranchRef" \ "target" \ "history" \ "edges") \\ "node"
      val authors = nodes.map(author => ((author \ "author" \ "name").as[String], (author \ "author" \ "email").as[String]))

      // Count commiters by grouping them, then sort them and take the first 10 to finally construct a Commiter
      val topCommiters = authors.groupBy(a => a).mapValues(_.size).toList.sortBy(_._2).reverse.take(10).map(a => Commiter(a._1, a._2))

      Ok(Json.toJson(topCommiters))
    })
  }


  /**
    * US 2-2 : Return top 10 languages used by a specific Github user based on its different projects
    */
  def getTopLanguages(userName: String): Action[AnyContent] = Action.async {

    val futureRepoNames: Future[WSResponse] = githubResourceHandler.userRepos(userName)

    val futureRepoNameList: Future[List[String]] = futureRepoNames.map {
      res => {
        val repoNames = (res.json \\ "full_name").map(repo => repo.as[String])
        // Here we can filter the forked repositories to have more personal statistics
        // To simplify, all repositories are used
        repoNames.toList
      }
    }

    // Get stats of all languages for all repositories
    val futureReposLangs: Future[List[Future[WSResponse]]] = futureRepoNameList.map {
      reposNames =>
        reposNames.map {
          repoFullName => {
            githubResourceHandler.topLanguages(repoFullName)
          }
        }
    }
    val futureReposLangsSequence: Future[List[WSResponse]] = futureReposLangs.map {
      res => {
        Future.sequence(res)
      }
    }.flatten

    futureReposLangsSequence.map {
      res => {
        val langList: List[Map[String, Long]] = res.map(a => {
          val langMap = a.json.as[JsObject]
          val m: Map[String, Long] = langMap.value.map {
            case (l, n) => (l, n.as[Long])
          }
          m
        })

        // TODO : do it simpler by summing languages data directly in a Map
        // Create a map containing only redundant languages to sum them
        val mapOfMerged = langList.reduce((m1, m2) => {
          m1.flatMap { case (l, n) =>
            m2.get(l).map(m => Map((l, n + m))).getOrElse(Map.empty[String, Long])
          }
        })

        val listOfAll = langList.flatMap(a => a.toList)
        // Create a map of non-redundant languages
        val mapOfAllExcludeMerged = listOfAll.filter(a => !mapOfMerged.contains(a._1)).toMap
        // Merge it with the map containing only summed languages, then sort and take the top 10
        val mapMerged = (mapOfMerged ++ mapOfAllExcludeMerged).toList.sortBy(_._2).reverse.take(10)

        Ok(Json.toJson(mapMerged))
      }
    }
  }


  /**
    * US 2-3 : Return a map of the number of issues for the past 30 days of a specific Github project
    */
  def getIssues(repoOwner: String, repoName: String): Action[AnyContent] = Action.async {
    val dayHistorySize = 30
    val issuesMinDate = DateUtil.addDaysToDate(DateUtil.currentDate(), -dayHistorySize)

    val futureRepoIssues: Future[WSResponse] = githubResourceHandler.issues(repoOwner, repoName, issuesMinDate)

    val futureSortedIssuesDate = futureRepoIssues.map {
      res => {
        // Navigate GraphQL date structure to extract a map of dates
        val nodes = (res.json \ "data" \ "search" \ "nodes") \\ "createdAt"
        val issueDates = nodes.map(repo => repo.as[String]).map(d => DateUtil.convertGithubDate(d))
        val issuesPerDay = issueDates.groupBy(a => a).mapValues(_.size)

        // Fill the map holes with a date and a 0 for the issues number, then sort the map by date
        val filledIssuesDates = DateUtil.fillDatesMapWithZeros(issuesPerDay, issuesMinDate, dayHistorySize)
        val sortedIssuesDates = ListMap(filledIssuesDates.toSeq.sortBy(_._1): _*)
        sortedIssuesDates
      }
    }
    futureSortedIssuesDate.map(res => Ok(Json.toJson(res)))
  }
}
