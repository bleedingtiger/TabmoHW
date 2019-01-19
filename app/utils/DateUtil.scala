package utils

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar

object DateUtil {
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
  val githubDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
  val calendar: Calendar = Calendar.getInstance

  def convertGithubDate(githubDate: String): String = {
    dateFormat.format(githubDateFormat.parse(githubDate))
  }

  def currentDate(): String = {
    dateFormat.format(java.sql.Date.valueOf(LocalDate.now()))
  }

  def addDaysToDate(date: String, days: Int): String = {
    calendar.setTime(dateFormat.parse(date))
    calendar.add(Calendar.DATE, days)
    dateFormat.format(calendar.getTime)
  }

  def datesMapWithZeros(startDate: String, days: Int): Map[String, Int] = {
    (for {
      i <- 0 until days
      d = {
        calendar.setTime(dateFormat.parse(startDate))
        calendar.add(Calendar.DATE, i)
        dateFormat.format(calendar.getTime)
      }
    } yield d -> 0)(collection.breakOut[Any, (String, Int), Map[String, Int]])
  }

  /**
    * Fills a map of dates with zeros where there are holes, between a start date and a duration in days
    * @param dateMap Map[String, Int] of string dates containing holes
    * @param startDate  Starting date
    * @param days Number of days to go from the starting date, positive
    */
  def fillDatesMapWithZeros(dateMap: Map[String, Int], startDate: String, days: Int): Map[String, Int] = {
    val zerosMap = datesMapWithZeros(startDate, days)
    zerosMap.map {
      case (d, c) => (d, {
        if(dateMap.contains(d)) dateMap(d) + c else c
      })
    }
  }
}
