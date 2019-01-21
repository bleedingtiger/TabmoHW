package controllers.github

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

/**
  * This singleton manages Github data by sendi,g requests to its API
  * GraphQL is used as many as possible, but sometimes lack of data,
  * in these cases the REST API is used
  */
@Singleton
class GithubResourceHandler @Inject()()(implicit config: Configuration, ws: WSClient, ec: ExecutionContext) {

  /**
    * Send a GraphQL request to Github to get all commiters (redundantly) of a project
    * @return a future containing the name and the email of every author of every commit on a specific project
    */
  def topCommiters(repoOwner: String, repoName: String): Future[WSResponse] = {
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
    ws.url(url).addHttpHeaders(("Authorization", "Bearer " + config.get[String]("token.githubGraphQL"))).post(data)
  }


  /**
    * Send a request to Github's REST API to get all repositories of a user
    * @return all data about every repository of a user
    */
  def userRepos(userName: String): Future[WSResponse] = {
    val url = s"https://api.github.com/users/$userName/repos"
    ws.url(url).get()
  }


  /**
    * Send a request to Github's REST API to get all languages used in a specific user's project
    * @param repoFullName must be a string of the form "username/repoName"
    * @return all data about languages used in a specific user's project
    */
  def topLanguages(repoFullName: String): Future[WSResponse] = {
    // Call Github's REST API because GraphQL API does not provide language statistics
    val url = s"https://api.github.com/repos/$repoFullName/languages"
    ws.url(url).get()
  }


  /**
    * Send a GraphQL request to Github to get the creation date of all issues
    * @param repoOwner repository owner's username
    * @param repoName repository's name
    * @param issuesMinDate the date (yyyy-MM-dd) from which we begin to count issues
    * @return
    */
  def issues(repoOwner: String, repoName: String, issuesMinDate: String): Future[WSResponse] = {
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
    ws.url(url).addHttpHeaders(("Authorization", "Bearer " + config.get[String]("token.githubGraphQL"))).post(data)
  }
}
