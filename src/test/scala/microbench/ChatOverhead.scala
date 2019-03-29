package microbench

import java.io.{BufferedWriter, FileWriter}

import au.com.bytecode.opencsv.CSVWriter
import chatapp.{Chat, ChatMessage, ChatUser, NonRoverChat}
import rover.rdo.client.RdObject
import rover.rdo.state.AtomicObjectState
import utilities.Utilities

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.util.Random


class ChatOverheadMicroBench(val numRepetitions: Int,
                             val maxMessageLength: Int,
                             var roverBenchDurations: List[Long] = List[Long](),
                             var nonRoverBenchDurations: List[Long] = List[Long]()) {


    val nonRoverChat: NonRoverChat = NonRoverChat.initial(List[ChatMessage]())
    val roverChat: Chat = Chat.fromRDO(new RdObject[List[ChatMessage]](AtomicObjectState.initial(List[ChatMessage]())))

    def benchmarkRoverChat(randomMessages: List[ChatMessage]): List[Long] = {
        var benchInit: Long = 0.asInstanceOf[Long]

        for (message <- randomMessages) {
            benchInit = System.nanoTime()
            roverChat.sendSynchronous(message)
            roverBenchDurations = roverBenchDurations :+ (System.nanoTime() - benchInit)
        }
        return roverBenchDurations
    }

    def benchmarkNonRoverChat(randomMessages: List[ChatMessage]): List[Long] = {
        var benchInit: Long = 0.asInstanceOf[Long]

        for (message <- randomMessages) {
            benchInit = System.nanoTime()
            nonRoverChat.send(message)
            nonRoverBenchDurations = nonRoverBenchDurations :+ (System.nanoTime() - benchInit)
        }
        return nonRoverBenchDurations
    }

    def run: Unit = {
        val randomMessages = Chat.generateRandomMessages(numRepetitions, maxMessageLength)
        val roverDurations = benchmarkRoverChat(randomMessages)
        val nonRoverDurations = benchmarkNonRoverChat(randomMessages)

        val csv = toCSV(randomMessages, nonRoverDurations, roverDurations)

        val meanRoverDurations = Utilities.getMean(roverDurations)
        val meanNonRoverDurations = Utilities.getMean(nonRoverDurations)
        println(s"RoverChat: mean time in nano: $meanRoverDurations, std: ${Utilities.getStd(roverDurations)}")
        println(s"NonRoverChat: mean time in nano $meanNonRoverDurations, std: ${Utilities.getStd(nonRoverDurations)}")

        println("\n")
        println(f"Overhead: ${Utilities.getOverhead(meanNonRoverDurations.asInstanceOf[Double], meanRoverDurations.asInstanceOf[Double])}%1.4f")

    }

    def toCSV(messages: List[ChatMessage],
             baselineDurations: List[Long],
             compareDurations: List[Long]): Unit = {

        val outputFile = new BufferedWriter(new FileWriter("./result.csv"))
        val csvWriter = new CSVWriter(outputFile)
        val csvHeader = Array("id", "messages", "nonRoverDurations", "roverDurations")
        var listOfRecords = new ListBuffer[Array[String]]()
        listOfRecords += csvHeader
        Range.inclusive(1, messages.length).foreach(i => {
               listOfRecords += Array(i.toString, messages(i-1).toString, baselineDurations(i-1).toString, compareDurations(i-1).toString)
        })
        csvWriter.writeAll(listOfRecords.toList)
        outputFile.close()
    }

}

object ChatOverheadMicroBench {
    def main(args: Array[String]): Unit = {

        val microBench = new ChatOverheadMicroBench(10, 100)
        microBench.run

    }
}
