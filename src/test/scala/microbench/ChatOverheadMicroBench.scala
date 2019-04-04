package microbench

import java.io.{BufferedWriter, FileWriter}

import au.com.bytecode.opencsv.CSVWriter
import chatapp.model.{Chat, ChatMessage, NonRoverChat}
import rover.rdo.state.AtomicObjectState
import utilities.Utilities

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer


class ChatOverheadMicroBench(val numRepetitions: Int,
                             val maxMessageLength: Int,
                             var roverBenchDurations: List[Long] = List[Long](),
                             var nonRoverBenchDurations: List[Long] = List[Long](),
                             var roverSizes: List[Long] = List[Long](),
                             var nonRoverSizes: List[Long] = List[Long]()) extends Serializable {


    val nonRoverChat: NonRoverChat = new NonRoverChat(null, List[ChatMessage]())
    val roverChat: Chat = new Chat(null, AtomicObjectState.initial(List[ChatMessage]()))

    def benchmarkRoverChat(randomMessages: List[ChatMessage]): Unit = {
        var benchInit: Long = 0.asInstanceOf[Long]

        for (message <- randomMessages) {
            benchInit = System.nanoTime()
            roverChat.sendSynchronous(message)
            roverBenchDurations = roverBenchDurations :+ (System.nanoTime() - benchInit)
            println(s"rover state: ${roverChat.getCheckpointedState().immutableState}")
        }
    }

    def benchmarkNonRoverChat(randomMessages: List[ChatMessage]): Unit = {
        var benchInit: Long = 0.asInstanceOf[Long]

        for (message <- randomMessages) {
            benchInit = System.nanoTime()
            nonRoverChat.send(message)
            nonRoverBenchDurations = nonRoverBenchDurations :+ (System.nanoTime() - benchInit)
            println(s"non-rover state: ${nonRoverChat.getCheckpointedState().last}")
        }
    }

    def getSizeOverhead(messages: List[ChatMessage]): Unit = {

//        Range.inclusive(1, numRepetitions).foreach(_ => {
//            val randomMessages = Chat.generateRandomMessages(numRepetitions, maxMessageLength)
//            val roverObject = new Chat(null, AtomicObjectState.initial(randomMessages))
//            val nonRoverObject = new NonRoverChat(null, randomMessages)
//
//            roverSizes = roverSizes :+ Utilities.sizeOf(roverObject.getCheckpointedState()).asInstanceOf[Long]
//            nonRoverSizes = nonRoverSizes :+ Utilities.sizeOf(nonRoverObject.getCheckpointedState()).asInstanceOf[Long]
//        })

        for (message <- messages) {
            val roverObject = new Chat(null, AtomicObjectState.initial(List[ChatMessage](message)))
            val nonRoverObject = new NonRoverChat(null, List[ChatMessage](message))

            roverSizes = roverSizes :+ Utilities.sizeOf(roverObject.getCheckpointedState()).asInstanceOf[Long]
            nonRoverSizes = nonRoverSizes :+ Utilities.sizeOf(nonRoverObject.getCheckpointedState()).asInstanceOf[Long]
        }

        val meanRoverSizes = Utilities.getMean(roverSizes.slice(2, numRepetitions))
        val meanNonRoverSizes = Utilities.getMean(nonRoverSizes.slice(2, numRepetitions))

        println(s"Mean rover size: $meanRoverSizes, with std: ${Utilities.getStd(roverSizes.slice(2, numRepetitions))}")
        println(s"Mean non-rover size: $meanNonRoverSizes, with std: ${Utilities.getStd(nonRoverSizes.slice(2, numRepetitions))}")

        println(s"Size-overhead: ${Utilities.getOverhead(meanNonRoverSizes, meanRoverSizes)}")

    }


    def toCSV(messages: List[ChatMessage]): Unit = {

        val outputFile = new BufferedWriter(new FileWriter(s"./results_chat_${java.time.Instant.now.getEpochSecond.toString}.csv"))
        val csvWriter = new CSVWriter(outputFile)
        val csvHeader = Array("id", "messages", "nonRoverDurations", "roverDurations", "nonRoverSizes", "roverSizes")
        var listOfRecords = new ListBuffer[Array[String]]()
        listOfRecords += csvHeader
        Range.inclusive(1, messages.length).foreach(i => {
               listOfRecords += Array(i.toString, messages(i-1).toString, nonRoverBenchDurations(i-1).toString,
                   roverBenchDurations(i-1).toString, nonRoverSizes(i-1).toString, roverSizes(i-1).toString)
        })
        csvWriter.writeAll(listOfRecords.toList)
        outputFile.close()
    }

    def run(): Unit = {
        val randomMessages = Chat.generateRandomMessages(numRepetitions, maxMessageLength)
        benchmarkRoverChat(randomMessages)
        benchmarkNonRoverChat(randomMessages)

        val meanRoverDurations = Utilities.getMean(this.roverBenchDurations.slice(2, numRepetitions))
        val meanNonRoverDurations = Utilities.getMean(this.nonRoverBenchDurations.slice(2, numRepetitions))

        println(s"RoverChat: mean time in nano: $meanRoverDurations, std: ${Utilities.getStd(this.roverBenchDurations.slice(2, numRepetitions))}")
        println(s"\t Ops/s: ${Utilities.oneSecInNano/meanRoverDurations}")
        println(s"NonRoverChat: mean time in nano $meanNonRoverDurations, std: ${Utilities.getStd(this.nonRoverBenchDurations.slice(2, numRepetitions))}")
        println(s"\t Ops/s: ${Utilities.oneSecInNano/meanNonRoverDurations}")

        println("\n")
        println(f"Overhead: ${Utilities.getOverhead(meanNonRoverDurations.asInstanceOf[Double], meanRoverDurations.asInstanceOf[Double])}%1.4f")

        println("\n")
        println("Benchmark Size overhead")
        this.getSizeOverhead(randomMessages)

        println("Generating CSV")
        this.toCSV(randomMessages)

    }

}

object ChatOverheadMicroBench {
    def main(args: Array[String]): Unit = {

        val microBench = new ChatOverheadMicroBench(5, 10)

        microBench.run()
//        println(s"Rover benches: ${microBench.roverBenchDurations}")
//        println(s"Non Rover benches: ${microBench.nonRoverBenchDurations}")

    }
}
