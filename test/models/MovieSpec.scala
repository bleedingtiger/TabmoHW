package models

import com.fasterxml.jackson.core.JsonParseException
import org.scalatestplus.play.PlaySpec
import play.api.libs.json._


class MovieSpec extends PlaySpec {

  "Movie.implicitReads: Reads[Movie]" must {
    "be successfully parsed when correct (minimal amount of fields)" in {
      val parsed = Json.parse("""{"title": "Hello, the movie", "country": "FRA", "year": 2019, "genre": ["sf", "animation"], "ranking": 7.1}""").as[Movie]
      val m = Movie("Hello, the movie", "FRA", 2019, None, None, None, List("sf", "animation"), 7.1f)
      parsed mustBe m
    }
    "be successfully parsed when correct (all fields)" in {
      val parsed = Json.parse(
        """{"title": "Hello, the movie", "country": "FRA", "year": 2019, "original_title": "A title",
          |"french_release": "2019/01/25", "synopsis": "A synopsis", "genre": ["sf", "animation"], "ranking": 7.1}""".stripMargin).as[Movie]
      val m = Movie("Hello, the movie", "FRA", 2019, Some("A title"), Some("2019/01/25"), Some("A synopsis"), List("sf", "animation"), 7.1f)
      parsed mustBe m
    }

    // title
    "fail to be parsed when no title is specified" in {
      a[JsonParseException] must be thrownBy Json.parse(""""country": "FRA", "year": 2019, "genre": ["sf", "animation"], "ranking": 7.1}""")
    }
    "fail to be parsed as a Movie when title is blank" in {
      val parsed = Json.parse("""{"title": "", "country": "FRA", "year": 2019, "genre": ["sf", "animation"], "ranking": 7.1}""")
      a[JsResultException] must be thrownBy parsed.as[Movie]
    }
    "fail to be parsed as a Movie when title exceeds 250 characters" in {
      val title = "A" * 251
      val parsed = Json.parse(s"""{"title": "$title", "country": "FRA", "year": 2019, "genre": ["sf", "animation"], "ranking": 7.1}""")
      a[JsResultException] must be thrownBy parsed.as[Movie]
    }

    // country
    "fail to be parsed when no country is specified" in {
      val parsed = Json.parse("""{"title": "Hello, the movie", "year": 2019, "genre": ["sf", "animation"], "ranking": 7.1}""")
      a[JsResultException] must be thrownBy parsed.as[Movie]
    }
    "fail to be parsed as a Movie when country is not specified" in {
      val parsed = Json.parse("""{"title": "Hello, the movie", "country": "", "year": 2019, "genre": ["sf", "animation"], "ranking": 7.1}""")
      a[JsResultException] must be thrownBy parsed.as[Movie]
    }
    "fail to be parsed as a Movie when country does not match Format ISO 3166-1 alpha-3" in {
      val parsed = Json.parse("""{"title": "Hello, the movie", "country": "FR0", "year": 2019, "genre": ["sf", "animation"], "ranking": 7.1}""")
      a[JsResultException] must be thrownBy parsed.as[Movie]
    }

    // year
    "fail to be parsed when no year is specified" in {
      val parsed = Json.parse("""{"title": "", "country": "FRA", "genre": ["sf", "animation"], "ranking": 7.1}""")
      a[JsResultException] must be thrownBy parsed.as[Movie]
    }
    "fail to be parsed when year is not specified" in {
      a[JsonParseException] must be thrownBy Json.parse("""{"title": "", "country": "FRA", "year": , "genre": ["sf", "animation"], "ranking": 7.1}""")
    }

    // original_title
    "fail to be parsed as a Movie when original_title exceeds 250 characters" in {
      val title = "A" * 251
      val parsed = Json.parse(s"""{"title": "", "country": "FRA", "year": 2019, "original_title": "$title", "genre": ["sf", "animation"], "ranking": 7.1}""")
      a[JsResultException] must be thrownBy parsed.as[Movie]
    }

    // french_release
    "fail to be parsed as a Movie when french_release is not a valid date" in {
      val parsed = Json.parse("""{"title": "", "country": "FRA", "year": 2019, "french_release": "2000/13/13", "genre": ["sf", "animation"], "ranking": 7.1}""")
      a[JsResultException] must be thrownBy parsed.as[Movie]
    }

    // synopsis
    "fail to be parsed as a Movie when synopsis exceeds 9999 characters" in {
      val synopsis = "A" * 10000
      val parsed = Json.parse(s"""{"title": "", "country": "FRA", "year": 2019, "synopsis": "$synopsis", "genre": ["sf", "animation"], "ranking": 7.1}""")
      a[JsResultException] must be thrownBy parsed.as[Movie]
    }

    // genre
    "fail to be parsed when no genre is specified" in {
      val parsed = Json.parse("""{"title": "", "country": "FRA", "year": 2019, "ranking": 7.1}""")
      a[JsResultException] must be thrownBy parsed.as[Movie]
    }
    "fail to be parsed when genre is an empty list" in {
      val parsed = Json.parse("""{"title": "", "country": "FRA", "year": 2019, "genre": [], "ranking": 7.1}""")
      a[JsResultException] must be thrownBy parsed.as[Movie]
    }
    "fail to be parsed as a Movie when a genre exceeds 9999 characters" in {
      val genre = "A" * 51
      val parsed = Json.parse(s"""{"title": "", "country": "FRA", "year": 2019, "genre": ["$genre"], "ranking": 7.1}""")
      a[JsResultException] must be thrownBy parsed.as[Movie]
    }

    // ranking
    "fail to be parsed when no ranking is specified" in {
      val parsed = Json.parse("""{"title": "", "country": "FRA", "year": 2019, "genre": ["sf", "animation"]}""")
      a[JsResultException] must be thrownBy parsed.as[Movie]
    }
    "fail to be parsed when year is not specified" in {
      val parsed = Json.parse("""{"title": "", "country": "FRA", "year": 2019, "genre": ["sf", "animation"], "ranking": }""")
      a[JsResultException] must be thrownBy parsed.as[Movie]
    }
    "fail to be parsed as a Movie when ranking is < 0" in {
      val ranking = -1
      val parsed = Json.parse(s"""{"title": "", "country": "FRA", "year": 2019, "genre": ["sf", "animation"], "ranking": $ranking}""")
      a[JsResultException] must be thrownBy parsed.as[Movie]
    }
    "fail to be parsed as a Movie when ranking is > 10" in {
      val ranking = 11
      val parsed = Json.parse(s"""{"title": "", "country": "FRA", "year": 2019, "genre": ["sf", "animation"], "ranking": $ranking}""")
      a[JsResultException] must be thrownBy parsed.as[Movie]
    }
    "fail to be parsed as a Movie when ranking is a multiple of 0.1" in {
      val ranking = 5.55
      val parsed = Json.parse(s"""{"title": "", "country": "FRA", "year": 2019, "genre": ["sf", "animation"], "ranking": $ranking}""")
      a[JsResultException] must be thrownBy parsed.as[Movie]
    }
  }

}
