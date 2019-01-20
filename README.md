# Homework Tabmo API

This project is the result of [this homework](https://github.com/tabmo/homework/blob/master/backend/api/index.md), done by Jean-Louis Kerfriden.

## Running

To start the API, make sure you have sbt installed and then run the command :

```bash
sbt run
```

Play Framework will start in development mode, allowing you to browse the API at <http://localhost:9000/>.




## Testing

Some unit tests have been writted for the API, you can run them with :

```bash
sbt test
```

## Usage

### Epic 1

##### US 1-1

In order to add a new Movie, you will need to send a POST request to <localhost:9000/api/mediatheque/movies> 
containing JSON of the form :

**Minimal fields** :

```
{"title": "Movie title", "country": "FRA", "year": 2000, "genre": ["Genre"], "ranking": 5}
```

**All possible fields** :

```
{"title": "Movie title", "country": "FRA", "year": 2000, "original_title": "Original title", "french_release": "YYYY/MM/DD", "synopsis": "Synopsis", "genre": ["Genre"], "ranking": 5}
```

To help you, the project contains a file with a JSON to add a simple movie, located at _/public/json/movie.json_ that you can send to the API using this command :

```bash
curl localhost:9000/api/mediatheque/movies -H "Content-type:application/json" -X POST -d @movie.json
```

**Note** : Some improvements must be done for this User Story, like adding custom Validators.

**US 1-2** : <http://localhost:9000/api/mediatheque/movies?genre=SF>

**US 1-3** : <http://localhost:9000/api/mediatheque/movies/groupByYear>

### Epic 2

Going to <http://localhost:9000/> will allow you to navigate to requests examples showing you what the API can do.

For the most lazy of you, here are different examples :

**US 2-1** : <http://localhost:9000/api/github/repos/scala/scala/commiters>

**US 2-2** : <http://localhost:9000/api/github/users/odersky/languages>

**US 2-3** : <http://localhost:9000/api/github/repos/angular/angular/issues>

**Important** : Github's GraphQL API needs a token to send requests, my personal public token is used but you can put your own token in the configuration file, located : _/conf/application.conf_

**Note** : Feel free to change the _username/reponame_ in the links to play with different Github data. 