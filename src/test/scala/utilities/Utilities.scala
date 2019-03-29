package utilities

object Utilities {
    def getMean(durations: List[Long]): Long = {
        return durations.sum / durations.length
    }

    def getStd(durations: List[Long]): Double = {
        val meanBenchTime = getMean(durations)
        val sqrt = math.sqrt(durations.foldLeft(0.asInstanceOf[Long])((total, current) =>
            total + (current - meanBenchTime) * (current - meanBenchTime)) / (durations.length -1))
        return sqrt
    }

    def getOverhead(meanBaselineDuration: Double, meanCompareDuration: Double) : Double = {
        return math.abs(meanBaselineDuration - meanCompareDuration) / meanBaselineDuration
    }
}
