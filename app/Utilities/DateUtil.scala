package Utilities

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.{Calendar, Date}

object DateUtil {
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
  val githubDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
  val calendar = Calendar.getInstance

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
}
