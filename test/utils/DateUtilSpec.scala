package utils

import org.scalatestplus.play.PlaySpec

class DateUtilSpec extends PlaySpec {

  "DateUtil.convertGithubDate()" must {
    "convert a date from yyyy-MM-dd'T'HH:mm:ss'Z' to yyyy-MM-dd" in {
      val githubDate = "2019-01-25T10:45:00Z"
      val convertedDate = utils.DateUtil.convertGithubDate(githubDate)
      val dateRegex = """([0-9]{4}-[0-9]{2}-[0-9]{2})"""
      convertedDate must fullyMatch regex dateRegex
    }
  }

  "DateUtil.currentDate()" must {
    "return a date of the form yyyy-MM-dd" in {
      val curDate = utils.DateUtil.currentDate()
      val dateRegex = """([0-9]{4}-[0-9]{2}-[0-9]{2})"""
      curDate must fullyMatch regex dateRegex
    }
  }

  "DateUtil.addDaysToDate()" must {
    "keep the same date if adding 0 days" in {
      val startDate = "2019-01-25"
      val newDate = utils.DateUtil.addDaysToDate(startDate, 0)
      newDate mustBe "2019-01-25"
    }
    "return a date 30 days in the future if we add 30 days" in {
      val startDate = "2019-01-25"
      val newDate = utils.DateUtil.addDaysToDate(startDate, 30)
      newDate mustBe "2019-02-24"
    }
    "return a date 30 days ago if we subtract 30 days" in {
      val startDate = "2019-01-25"
      val newDate = utils.DateUtil.addDaysToDate(startDate, -30)
      newDate mustBe "2018-12-26"
    }
  }

  "DateUtil.datesMapWithZeros()" must {
    "return a Map which contains as many keys as days" in {
      val startDate = "2019-01-25"
      val nDays = 30
      val dateMap = utils.DateUtil.datesMapWithZeros(startDate, nDays)
      dateMap.size mustBe nDays
    }
    "return a Map with dates of the form yyyy-MM-dd as key" in {
      val startDate = "2019-01-25"
      val nDays = 30
      val dateRegex = """([0-9]{4}-[0-9]{2}-[0-9]{2})"""
      val dateMap = utils.DateUtil.datesMapWithZeros(startDate, nDays)
      dateMap.map(a => a._1 must fullyMatch regex dateRegex)
    }
    "return a Map with 0 as value of every key" in {
      val startDate = "2019-01-25"
      val nDays = 30
      val dateMap = utils.DateUtil.datesMapWithZeros(startDate, nDays)
      dateMap.map(a => a._2 mustBe 0)
    }
  }

  "DateUtil.fillDatesMapWithZeros()" must {
    "fills the holes with zeros where key does not exist from start date to duration" in {
      val dateMap = Map(("2019-01-28", 2), ("2019-01-30", 5), ("2019-02-02", 1))
      val startDate = "2019-01-25"
      val nDays = 10
      val filledDateMap = utils.DateUtil.fillDatesMapWithZeros(dateMap, startDate, nDays)
      filledDateMap.contains("2019-01-24") mustBe false
      filledDateMap.contains("2019-01-25") mustBe true
      filledDateMap.contains("2019-01-26") mustBe true
      filledDateMap.contains("2019-01-28") mustBe true
      filledDateMap.contains("2019-02-03") mustBe true
      filledDateMap.contains("2019-02-04") mustBe false
      filledDateMap("2019-01-25") mustBe 0
      filledDateMap("2019-01-26") mustBe 0
      filledDateMap("2019-01-28") mustBe 2
      filledDateMap("2019-02-03") mustBe 0
    }
  }
}
