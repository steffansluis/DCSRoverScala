package utilities

import scala.math.{pow, sqrt}

class Results(private val values: List[Long] = List[Long]()) {

	def addResult(result: Long): Results = {
		return new Results(values :+ result)
	}

	lazy val stdDev: Double = {
		val meanOfSquares = values.map(pow(_, 2)).sum / values.size
		val squareOfMean = pow(mean, 2)

		sqrt(meanOfSquares - squareOfMean)
	}

	lazy val mean: Double = {
		values.sum / values.size
	}
}
