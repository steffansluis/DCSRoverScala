package microbench

import java.io.{BufferedWriter, ByteArrayOutputStream, FileWriter, ObjectOutputStream}

import au.com.bytecode.opencsv.CSVWriter
import chatapp.{Chat, ChatMessage, NonRoverChat}
import rover.rdo.client.RdObject
import rover.rdo.state.AtomicObjectState
import utilities.Utilities

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer


class ChatOverheadMicroBench(val numRepetitions: Int,
                             val maxMessageLength: Int,
                             var roverBenchDurations: List[Long] = List[Long](),
                             var nonRoverBenchDurations: List[Long] = List[Long]()) extends Serializable {


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

    def getSizeOverhead = {
        var roverSizes = List[Long]()
        var nonRoverSizes = List[Long]()

        Range.inclusive(1, numRepetitions).foreach(_ => {
            val randomMessages = Chat.generateRandomMessages(numRepetitions, maxMessageLength)
            val roverObject = Chat.fromRDO(new RdObject[List[ChatMessage]](AtomicObjectState.initial(randomMessages)))
            val nonRoverObject = NonRoverChat.initial(randomMessages)

            roverSizes = roverSizes :+ Utilities.sizeOf(roverObject).asInstanceOf[Long]
//            println(s"rover-size: ${roverSizes.last}")
            nonRoverSizes = nonRoverSizes :+ Utilities.sizeOf(nonRoverObject).asInstanceOf[Long]
//            println(s"non-rover-size: ${nonRoverSizes.last}")
        })

        println(s"Mean rover size: ${Utilities.getMean(roverSizes)}, with std: ${Utilities.getStd(roverSizes)}")
        println(s"Mean non-rover size: ${Utilities.getMean(nonRoverSizes)}, with std: ${Utilities.getStd(nonRoverSizes)}")

        println(s"Size-overhead: ${Utilities.getOverhead(Utilities.getMean(nonRoverSizes), Utilities.getMean(roverSizes))}")

    }


    def run: Unit = {
        val randomMessages = Chat.generateRandomMessages(numRepetitions, maxMessageLength)
        val roverDurations = benchmarkRoverChat(randomMessages)
        val nonRoverDurations = benchmarkNonRoverChat(randomMessages)

        val csv = toCSV(randomMessages, nonRoverDurations, roverDurations)

        val meanRoverDurations = Utilities.getMean(roverDurations.slice(1, numRepetitions))
        val meanNonRoverDurations = Utilities.getMean(nonRoverDurations.slice(1, numRepetitions))
        println(s"RoverChat: mean time in nano: $meanRoverDurations, std: ${Utilities.getStd(roverDurations.slice(1, numRepetitions))}")
        println(s"\t Ops/s: ${Utilities.oneSecInNano/meanRoverDurations}")
        println(s"NonRoverChat: mean time in nano $meanNonRoverDurations, std: ${Utilities.getStd(nonRoverDurations.slice(1, numRepetitions))}")
        println(s"\t Ops/s: ${Utilities.oneSecInNano/meanNonRoverDurations}")

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

        val microBench = new ChatOverheadMicroBench(100, 100)

        println("Benchmark time overhead")
        microBench.run
        println(s"Rover benches: ${microBench.roverBenchDurations}")
        println(s"Non Rover benches: ${microBench.nonRoverBenchDurations}")

        println("\n\n")
        println("Benchmark Size overhead")
        microBench.getSizeOverhead
    }
}
