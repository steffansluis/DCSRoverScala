package microbench

import utilities.Utilities
import votingapp.{NonRoverPoll, Poll}

import scala.util.Random

class PollOverheadMicroBench(val numRepetitions: Int,
                             val numChoices: Int,
                             var roverBenchDurations: List[Long] = List[Long](),
                             var nonRoverBenchDurations: List[Long] = List[Long]()) {

    val roverPoll = Poll("microBenchPoll", numChoices)
    val nonRoverPoll = Poll.toNonRoverInit(roverPoll)

    def benchmarkRoverPollCast(): List[Long] = {

        Range.inclusive(1, numRepetitions).foreach(_ => {
            var benchInit = System.nanoTime()
            roverPoll.cast(roverPoll.choices(math.round((numChoices - 1) * Random.nextFloat())))
            roverBenchDurations = roverBenchDurations :+ (System.nanoTime() - benchInit)
        })
        return roverBenchDurations
    }

    def benchmarkRoverPollResult(): List[Long]  = {
        Range.inclusive(1, numRepetitions).foreach(_ => {
            var poll = Poll.generateRandom("microBenchPollResult", numChoices)
            var benchInit = System.nanoTime()
            poll.result.winner
            roverBenchDurations = roverBenchDurations :+ (System.nanoTime() - benchInit)
        })
        return roverBenchDurations
    }

    def benchmarkNonRoverPollCast(): List[Long] = {
        for (i <- Range.inclusive(1, numRepetitions)) {
            var benchInit = System.nanoTime()
            nonRoverPoll.cast(nonRoverPoll.choices(math.round((numChoices - 1) * Random.nextFloat())))
            roverBenchDurations = roverBenchDurations :+ (System.nanoTime() - benchInit)
        }
        return roverBenchDurations
    }

    def benchmarkNonRoverPollResult(): List[Long] = {
        Range.inclusive(1, numRepetitions).foreach(_ => {
            var poll = NonRoverPoll.generateRandom("microBenchPollResult", numChoices)
            var benchInit = System.nanoTime()
            poll.result.winner
            roverBenchDurations = roverBenchDurations :+ (System.nanoTime() - benchInit)
        })
        return roverBenchDurations
    }

    def runCast(): Unit = {
        val roverDurations = benchmarkRoverPollCast()
        val nonRoverDurations = benchmarkNonRoverPollCast()

//        val csv = toCSV(randomMessages, nonRoverDurations, roverDurations)

        val meanRoverDurations =  Utilities.getMean(roverDurations)
        val meanNonRoverDurations = Utilities.getMean(nonRoverDurations)
        println(s"RoverPollCast: mean time in nano: $meanRoverDurations, std: ${Utilities.getStd(roverDurations)}")
        println(s"NonRoverPollCast: mean time in nano $meanNonRoverDurations, std: ${Utilities.getStd(nonRoverDurations)}")

        println("\n")
        println(f"Overhead: ${Utilities.getOverhead(meanNonRoverDurations.asInstanceOf[Double], meanRoverDurations.asInstanceOf[Double])}%1.4f")
    }

    def runResult(): Unit = {
        val roverDurations = benchmarkNonRoverPollResult()
        val nonRoverDurations = benchmarkNonRoverPollResult()

        //        val csv = toCSV(randomMessages, nonRoverDurations, roverDurations)

        val meanRoverDurations =  Utilities.getMean(roverDurations)
        val meanNonRoverDurations = Utilities.getMean(nonRoverDurations)
        println(s"RoverPollResult: mean time in nano: $meanRoverDurations, std: ${Utilities.getStd(roverDurations)}")
        println(s"NonRoverPollResult: mean time in nano $meanNonRoverDurations, std: ${Utilities.getStd(nonRoverDurations)}")

        println("\n")
        println(f"Overhead: ${Utilities.getOverhead(meanNonRoverDurations.asInstanceOf[Double], meanRoverDurations.asInstanceOf[Double])}%1.4f")

    }
}

object PollOverheadMicroBench {
    def main(args: Array[String]) = {
        val microBench = new PollOverheadMicroBench(100, 10)
        microBench.runCast()

        println("\n\n")
        microBench.runResult()
    }
}
