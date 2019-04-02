package microbench

import utilities.Utilities
import votingapp.{NonRoverPoll, Poll}

class PollOverheadMicroBench(val numRepetitions: Int,
                             val numChoices: Int,
                             var roverBenchDurations: List[Long] = List[Long](),
                             var nonRoverBenchDurations: List[Long] = List[Long]()) {

    val roverPoll = Poll("microBenchPoll", numChoices)
    val nonRoverPoll = Poll.toNonRover(roverPoll)

    def benchmarkRoverPollCast(choiceIds: List[Int]): List[Long] = {

        for (choiceId <- choiceIds) {
            var benchInit = System.nanoTime()
            roverPoll.cast(roverPoll.choices(choiceId))
            roverBenchDurations = roverBenchDurations :+ (System.nanoTime() - benchInit)
        }
        return roverBenchDurations
    }

    def benchmarkNonRoverPollCast(choiceIds: List[Int]): List[Long] = {
        for (choiceId <- choiceIds) {
            var benchInit = System.nanoTime()
            nonRoverPoll.cast(nonRoverPoll.choices(choiceId))
            nonRoverBenchDurations = nonRoverBenchDurations :+ (System.nanoTime() - benchInit)
        }
        return nonRoverBenchDurations
    }

    def benchmarkRoverPollResult(polls: List[Poll]): List[Long]  = {
        for (poll <- polls) {
            var benchInit = System.nanoTime()
            poll.result.winner
            roverBenchDurations = roverBenchDurations :+ (System.nanoTime() - benchInit)
        }
        return roverBenchDurations
    }

    def benchmarkNonRoverPollResult(polls: List[NonRoverPoll]): List[Long] = {
        for (poll <- polls) {
            var benchInit = System.nanoTime()
            poll.result.winner
            nonRoverBenchDurations = nonRoverBenchDurations :+ (System.nanoTime() - benchInit)
        }
        return nonRoverBenchDurations
    }

    def getSizeOverhead() = {
        val roverPolls = Poll.generateRandomPolls("getSizeOverhead", numChoices, numRepetitions)
        val nonRoverPolls = Poll.toNonRoverRandomPolls(roverPolls)

        var roverSizes = List[Long]()
        var nonRoverSizes = List[Long]()

        Range.inclusive(0, numRepetitions-1).foreach(pollId => {
            roverSizes = roverSizes :+ Utilities.sizeOf(roverPolls(pollId)).asInstanceOf[Long]
            nonRoverSizes = nonRoverSizes :+ Utilities.sizeOf(nonRoverPolls(pollId)).asInstanceOf[Long]
        })

        println(s"Mean rover size: ${Utilities.getMean(roverSizes)}, with std: ${Utilities.getStd(roverSizes)}")
        println(s"Mean non-rover size: ${Utilities.getMean(nonRoverSizes)}, with std: ${Utilities.getStd(nonRoverSizes)}")

        println(s"Size-overhead: ${Utilities.getOverhead(Utilities.getMean(nonRoverSizes), Utilities.getMean(roverSizes))}")
    }

    def runCast(): Unit = {
        val randomChoices = Utilities.generateRandomInts(numRepetitions, numChoices)

        val roverDurations = benchmarkRoverPollCast(randomChoices)
        val nonRoverDurations = benchmarkNonRoverPollCast(randomChoices)

        val meanRoverDurations =  Utilities.getMean(roverDurations.slice(1, numRepetitions))
        val meanNonRoverDurations = Utilities.getMean(nonRoverDurations.slice(1, numRepetitions))

        println(s"RoverPollCast: mean time in nano: $meanRoverDurations, std: ${Utilities.getStd(roverDurations.slice(1, numRepetitions))}")
        println(s"\t Ops/s: ${Utilities.oneSecInNano/meanRoverDurations}")

        println(s"NonRoverPollCast: mean time in nano $meanNonRoverDurations, std: ${Utilities.getStd(nonRoverDurations.slice(1, numRepetitions))}")
        println(s"\t Ops/s: ${Utilities.oneSecInNano/meanNonRoverDurations}")


        println("\n")
        println(f"Overhead: ${Utilities.getOverhead(meanNonRoverDurations.asInstanceOf[Double], meanRoverDurations.asInstanceOf[Double])}%1.4f")
    }

    def runResult(): Unit = {
        val randomPoll = Poll.generateRandom("microBenchPollResult", numChoices)
        val randomPolls = Poll.generateRandomPolls("microBenchPollResult", numChoices, numRepetitions)

        val roverDurations = benchmarkRoverPollResult(randomPolls)
//        println(s"Rover votes: ${randomPolls.last.state.immutableState}")
        val nonRoverDurations = benchmarkNonRoverPollResult(Poll.toNonRoverRandomPolls(randomPolls))
//        println(s"Non Rover state: ${Poll.toNonRoverRandomPolls(randomPolls).last.votes}")


        val meanRoverDurations =  Utilities.getMean(roverDurations.slice(1, numRepetitions))
        val meanNonRoverDurations = Utilities.getMean(nonRoverDurations.slice(1, numRepetitions))
        println(s"RoverPollResult: mean time in nano: $meanRoverDurations, std: ${Utilities.getStd(roverDurations.slice(1, numRepetitions))}")
        println(s"\t Ops/s: ${Utilities.oneSecInNano/meanRoverDurations}")

        println(s"NonRoverPollResult: mean time in nano $meanNonRoverDurations, std: ${Utilities.getStd(nonRoverDurations.slice(1, numRepetitions))}")
        println(s"\t Ops/s: ${Utilities.oneSecInNano/meanNonRoverDurations}")


        println("\n")
        println(f"Overhead: ${Utilities.getOverhead(meanNonRoverDurations.asInstanceOf[Double], meanRoverDurations.asInstanceOf[Double])}%1.4f")

    }
}

object PollOverheadMicroBench {
    def main(args: Array[String]) = {
        val microBench = new PollOverheadMicroBench(100, 100)

        println("Benchmark vote casting")
        microBench.runCast()
        println(s"Rover benches: ${microBench.roverBenchDurations}")
        println(s"Non Rover benches: ${microBench.nonRoverBenchDurations}")

        println("\n\n")
        println("Benchmark pollresult")
        microBench.runResult()
        println(s"Rover benches: ${microBench.roverBenchDurations}")
        println(s"Non Rover benches: ${microBench.nonRoverBenchDurations}")

        println("\n\n")
        println("Benchmark size overhead")
        microBench.getSizeOverhead()

    }
}
