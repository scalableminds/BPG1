package projectZoom.connector.util

object Levenshtein {
  
  def similarity(str1: String, str2: String): Double = {
    1 - (distance(str1, str2).toFloat/(max(str1.size, str2.size)))
  }
  
  def distance(str1: String, str2: String): Int = {
    val lenStr1 = str1.length
    val lenStr2 = str2.length
 
    val d: Array[Array[Int]] = Array.ofDim(lenStr1 + 1, lenStr2 + 1)
 
    for (i <- 0 to lenStr1) d(i)(0) = i
    for (j <- 0 to lenStr2) d(0)(j) = j
 
    for (i <- 1 to lenStr1; j <- 1 to lenStr2) {
      val cost = if (str1(i - 1) == str2(j-1)) 0 else 1
 
      d(i)(j) = min(
        d(i-1)(j  ) + 1,     // deletion
        d(i  )(j-1) + 1,     // insertion
        d(i-1)(j-1) + cost   // substitution
      )
    }
 
    d(lenStr1)(lenStr2)
  }
 
  def min(nums: Int*): Int = nums.min
  def max(nums: Int*): Int = nums.max
}