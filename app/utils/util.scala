package utils

import java.util.Date

object util {
  def getAge(date: Date): Int = {
    val today = new Date().getTime
    val day = date.getTime
    val diff = today - day
    Math.floor(diff/60*60*24*365).toInt
  }

}
