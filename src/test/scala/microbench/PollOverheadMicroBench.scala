package microbench

import java.io.{BufferedWriter, FileWriter}

import au.com.bytecode.opencsv.CSVWriter
import utilities.Utilities
import votingapp.{NonRoverPoll, Poll}

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

class PollOverheadMicroBench(val numRepetitions: Int,
                             val numChoices: Int,
                             var roverCastDurations: List[Long] = List[Long](),
                             var nonRoverCastDurations: List[Long] = List[Long](),
                             var roverResultDurations: List[Long] = List[Long](),
                             var nonRoverResultDurations: List[Long] = List[Long](),
                             var roverSizes: List[Long] = List[Long](),
                             var nonRoverSizes: List[Long] = List[Long]()) {

    val roverPoll = Poll("microBenchPoll", numChoices)
    val nonRoverPoll = Poll.toNonRover(roverPoll)

    val randomChoices = Utilities.generateRandomInts(numRepetitions, numChoices)

    def benchmarkRoverPollCast(choiceIds: List[Int]): Unit = {
        for (choiceId <- choiceIds) {
            var benchInit = System.nanoTime()
            roverPoll.cast(roverPoll.choices(choiceId))
            roverCastDurations = roverCastDurations :+ (System.nanoTime() - benchInit)
            println(s"rover-state: ${roverPoll.state.immutableState}")

        }
    }

    def benchmarkNonRoverPollCast(choiceIds: List[Int]): Unit = {
        for (choiceId <- choiceIds) {
            var benchInit = System.nanoTime()
//            nonRoverPoll.cast(nonRoverPoll.choices(choiceId))
            nonRoverPoll.votes.add(nonRoverPoll.choices(choiceId))
            nonRoverCastDurations = nonRoverCastDurations :+ (System.nanoTime() - benchInit)
            println(s"non-rover-state: ${nonRoverPoll.votes}")
        }
    }

    def benchmarkRoverPollResult(polls: List[Poll]): Unit  = {
        for (poll <- polls) {
            var benchInit = System.nanoTime()
            poll.result.winner
            roverResultDurations = roverResultDurations :+ (System.nanoTime() - benchInit)
        }
    }

    def benchmarkNonRoverPollResult(polls: List[NonRoverPoll]): Unit = {
        for (poll <- polls) {
            var benchInit = System.nanoTime()
            poll.result.winner
            nonRoverResultDurations = nonRoverResultDurations :+ (System.nanoTime() - benchInit)
        }
    }

    def getSizeOverhead(): Unit = {
//        val roverPolls = Poll.generateRandomPolls("getSizeOverhead", numChoices, numRepetitions)
//        val nonRoverPolls = Poll.toNonRoverRandomPolls(roverPolls)

        for (choiceId <- randomChoices) {
            val roverObject = Poll.generateRandom("getSizeOverhead", numChoices)
            val nonRoverObject = Poll.toNonRover(roverObject)

            println(s"rover state: ${roverObject.state.immutableState}")
            println(s"non-rover state: ${nonRoverObject.votes}")

            roverObject.cast(roverObject.choices(choiceId))
            nonRoverObject.cast(nonRoverObject.choices(choiceId))

            println(s"rover new state: ${roverObject.state.immutableState}")
            println(s"non-rover new state: ${nonRoverObject.votes}")

            roverSizes = roverSizes :+ Utilities.sizeOf(roverObject.state).asInstanceOf[Long]
            nonRoverSizes = nonRoverSizes :+ Utilities.sizeOf(nonRoverObject.votes).asInstanceOf[Long]
        }

//        Range.inclusive(0, numRepetitions-1).foreach(pollId => {
//            roverSizes = roverSizes :+ Utilities.sizeOf(roverPolls(pollId).state).asInstanceOf[Long]
//            nonRoverSizes = nonRoverSizes :+ Utilities.sizeOf(nonRoverPolls(pollId).votes).asInstanceOf[Long]
//        })

        println(s"Mean rover size: ${Utilities.getMean(roverSizes)}, with std: ${Utilities.getStd(roverSizes)}")
        println(s"Mean non-rover size: ${Utilities.getMean(nonRoverSizes)}, with std: ${Utilities.getStd(nonRoverSizes)}")

        println(s"Size-overhead: ${Utilities.getOverhead(Utilities.getMean(nonRoverSizes), Utilities.getMean(roverSizes))}")
    }

    def runCast(): Unit = {

        benchmarkRoverPollCast(randomChoices)
        benchmarkNonRoverPollCast(randomChoices)

        val meanRoverDurations =  Utilities.getMean(roverCastDurations.slice(2, numRepetitions))
        val meanNonRoverDurations = Utilities.getMean(nonRoverCastDurations.slice(2, numRepetitions))

        println(s"RoverPollCast: mean time in nano: $meanRoverDurations, std: ${Utilities.getStd(roverCastDurations.slice(2, numRepetitions))}")
        println(s"\t Ops/s: ${Utilities.oneSecInNano/meanRoverDurations}")

        println(s"NonRoverPollCast: mean time in nano $meanNonRoverDurations, std: ${Utilities.getStd(nonRoverCastDurations.slice(2, numRepetitions))}")
        println(s"\t Ops/s: ${Utilities.oneSecInNano/meanNonRoverDurations}")


        println("\n")
        println(f"Overhead: ${Utilities.getOverhead(meanNonRoverDurations.asInstanceOf[Double], meanRoverDurations.asInstanceOf[Double])}%1.4f")
    }

    def runResult(): Unit = {
        val randomPolls = Poll.generateRandomPolls("microBenchPollResult", numChoices, numRepetitions)

        benchmarkRoverPollResult(randomPolls)
        benchmarkNonRoverPollResult(Poll.toNonRoverRandomPolls(randomPolls))

        val meanRoverDurations =  Utilities.getMean(roverResultDurations.slice(2, numRepetitions))
        val meanNonRoverDurations = Utilities.getMean(nonRoverResultDurations.slice(2, numRepetitions))

        println(s"RoverPollResult: mean time in nano: $meanRoverDurations, std: ${Utilities.getStd(roverResultDurations.slice(2, numRepetitions))}")
        println(s"\t Ops/s: ${Utilities.oneSecInNano/meanRoverDurations}")

        println(s"NonRoverPollResult: mean time in nano $meanNonRoverDurations, std: ${Utilities.getStd(nonRoverResultDurations.slice(2, numRepetitions))}")
        println(s"\t Ops/s: ${Utilities.oneSecInNano/meanNonRoverDurations}")


        println("\n")
        println(f"Overhead: ${Utilities.getOverhead(meanNonRoverDurations.asInstanceOf[Double], meanRoverDurations.asInstanceOf[Double])}%1.4f")

    }

    def toCSV(): Unit = {

        val outputFile = new BufferedWriter(new FileWriter(s"./results_poll_${java.time.Instant.now.getEpochSecond.toString}.csv"))
        val csvWriter = new CSVWriter(outputFile)
        val csvHeader = Array("id", "nonRoverCastDurations", "roverCastDurations", "nonRoverResultDurations", "roverResultDurations", "nonRoverSizes", "roverSizes")
        var listOfRecords = new ListBuffer[Array[String]]()
        listOfRecords += csvHeader
        Range.inclusive(1, numRepetitions).foreach(i => {
            listOfRecords += Array(i.toString, nonRoverCastDurations(i-1).toString, roverCastDurations(i-1).toString,
                nonRoverResultDurations(i-1).toString, roverResultDurations(i-1).toString,
                nonRoverSizes(i-1).toString, roverSizes(i-1).toString)
        })
        csvWriter.writeAll(listOfRecords.toList)
        outputFile.close()
    }

    def run(): Unit = {

        println("Benchmark Poll casting")
        runCast()

        println("\n")
        println("Benchmark Poll result")
        runResult()

        println("\n")
        println("Benchmark size overhead")
        getSizeOverhead()

        println("\n")
        println("Generating CSV")
        toCSV()
    }
}

object PollOverheadMicroBench {
    def main(args: Array[String]) = {
        val microBench = new PollOverheadMicroBench(5, 3)

        microBench.run()

    }
}
