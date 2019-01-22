package utils

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar

object DateUtil {
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
  val movieDateRegex = """([12]\d{3}/(0[1-9]|1[0-2])/(0[1-9]|[12]\d|3[01]))"""
  val githubDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
  val calendar: Calendar = Calendar.getInstance

  /**
    * Convert a date from the format (yyyy-MM-ddTHH:mm:ssZ) to (yyyy-MM-dd)
    */
  def convertGithubDate(githubDate: String): String = {
    dateFormat.format(githubDateFormat.parse(githubDate))
  }

  /**
    * Return the current date based on the local machine time
    */
  def currentDate(): String = {
    dateFormat.format(java.sql.Date.valueOf(LocalDate.now()))
  }

  /**
    * Add days to a date
    *
    * @param date base date
    * @param days number of days to add to the base date
    * @return a date, "days" day(s) in the futur if "days">0, in the past if "days"<0, or the same date if "days"==0
    */
  def addDaysToDate(date: String, days: Int): String = {
    calendar.setTime(dateFormat.parse(date))
    calendar.add(Calendar.DATE, days)
    dateFormat.format(calendar.getTime)
  }

  /**
    * Create and return a Map of (date -> 0) from the startDate to the startDate + days
    */
  def datesMapWithZeros(startDate: String, days: Int): Map[String, Int] = {
    (for {
      i <- 0 until days
      d = {
        calendar.setTime(dateFormat.parse(startDate))
        calendar.add(Calendar.DATE, i)
        dateFormat.format(calendar.getTime)
      }
    } yield d -> 0) (collection.breakOut[Any, (String, Int), Map[String, Int]])
  }

  /**
    * Fills a map of dates with zeros where there are holes, between a start date and a duration in days
    *
    * @param dateMap   Map[String, Int] of string dates containing holes
    * @param startDate Starting date
    * @param days      Number of days to go from the starting date, positive
    */
  def fillDatesMapWithZeros(dateMap: Map[String, Int], startDate: String, days: Int): Map[String, Int] = {
    val zerosMap = datesMapWithZeros(startDate, days)
    zerosMap.map {
      case (d, c) => (d, {
        if (dateMap.contains(d)) dateMap(d) + c else c
      })
    }
  }
}
