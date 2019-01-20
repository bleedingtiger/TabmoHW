package controllers.github

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GithubResourceHandler @Inject()()(implicit config: Configuration, ws: WSClient, ec: ExecutionContext) {

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


  def userRepos(userName: String): Future[WSResponse] = {
    val url = s"https://api.github.com/users/$userName/repos"
    ws.url(url).get()
  }


  def topLanguages(repoFullName: String): Future[WSResponse] = {
    // Call Github's REST API because GraphQL API does not provide language statistics
    val url = s"https://api.github.com/repos/$repoFullName/languages"
    ws.url(url).get()
  }


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
