package microbench

import java.io.{BufferedWriter, FileWriter}

import au.com.bytecode.opencsv.CSVWriter
import chatapp.ChatUser
import chatapp.model.{Chat, ChatMessage}
import utilities.{Results, Utilities}

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Random


class ChatOverheadMicroBench(val numRepetitions: Int,
                             val maxMessageLength: Int,
                             var nonRoverBenchDurations: List[Long] = List[Long]()) extends Serializable {

    def generateRandomMessages(numMessages: Int, maxMessageLength: Int): List[ChatMessage] = {
        var messageLength : Int = 0
        var messageBody: String = null
        var randomMessages = List[ChatMessage]()

        for (i <- Range.inclusive(1, numMessages)) {
            messageLength = Random.nextInt(maxMessageLength)
            messageBody = Random.alphanumeric.take(messageLength).mkString
            randomMessages = randomMessages :+ new ChatMessage(messageBody, ChatUser.System)
        }

        return randomMessages
    }

    /**
      * Determine (time) overhead of RdObject + AtomicObjectState
      * state modificaton mechanism
      */
    def benchmarkRdObjectChat(randomMessages: List[ChatMessage]): List[Long] = {

        val roverChat = Chat.initial()

        var timeTakenPerMessage = List[Long]()

        for (message <- randomMessages) {
            val tick = System.nanoTime()

            Await.ready(roverChat.send(message), Duration.Inf)

            val tock = System.nanoTime()
            timeTakenPerMessage = timeTakenPerMessage :+ (tock - tick)
        }

        return timeTakenPerMessage
    }

    def benchmarkPrimitiveChat(randomMessages: List[ChatMessage]): List[Long] = {
        var benchInit: Long = 0.asInstanceOf[Long]

        var nonRoverChat = List[ChatMessage]()

        for (message <- randomMessages) {
            val tick = System.nanoTime()

            nonRoverChat = nonRoverChat :+ message

            val tock = System.nanoTime()
            nonRoverBenchDurations = nonRoverBenchDurations :+ (tock - tick)
        }
        return nonRoverBenchDurations
    }

    def getSizeOverhead = {

        /* test data */
        val randomMessages = generateRandomMessages(numRepetitions, maxMessageLength)

        /* scenarios under test: */

        // The RdObject (its atomic state is measured, not the whole thing)
        val rdObject = Chat.initial()

        // Primitive chat (justa list of ChatMessages)
        var primitiveObject = List[ChatMessage]()

        /* For results */
        var rdoSizes = new Results
        var primitiveSizes = new Results

        for (msg <- randomMessages) {

            Await.ready(rdObject.send(msg), Duration.Inf)
            primitiveObject = primitiveObject :+ msg

            // todo: get delta? As new object will probably contain the previous?
            val memorySizeInBytesOfAtomicObjectState = Utilities.sizeOf(rdObject.state)
            val memorySizeInBytesOfPrimitveObjectState = Utilities.sizeOf(primitiveObject)

            rdoSizes = rdoSizes.addResult(memorySizeInBytesOfAtomicObjectState)
            primitiveSizes = primitiveSizes.addResult(memorySizeInBytesOfPrimitveObjectState)

            //println(s"rover-size: ${rdoSizes.last}")
            //println(s"non-rover-size: ${primitiveSizes.last}")
        }

        println(s"Mean rover size: ${rdoSizes.mean}, with std: ${rdoSizes.stdDev}")
        println(s"Mean primitive size: ${primitiveSizes.mean}, with std: ${primitiveSizes.stdDev}")

        println(s"Size-overhead: ${Utilities.getOverhead(primitiveSizes.mean, rdoSizes.mean)}")
    }


    def run: Unit = {
        val randomMessages = generateRandomMessages(numRepetitions, maxMessageLength)
        val roverDurations = benchmarkRdObjectChat(randomMessages)
        val nonRoverDurations = benchmarkPrimitiveChat(randomMessages)


        println("Duration per adding msg")
        println(s"   RdObject: ${roverDurations}")
        println(s"   Primitive: ${nonRoverDurations}")

        val csv = toCSV(randomMessages, nonRoverDurations, roverDurations)

        val meanRoverDurations = Utilities.getMean(roverDurations.slice(1, numRepetitions))
        val meanNonRoverDurations = Utilities.getMean(nonRoverDurations.slice(1, numRepetitions))

        println(s"RoverChat: mean time in nano: $meanRoverDurations, std: ${Utilities.getStd(roverDurations.slice(1, numRepetitions))}")
        println(s"    Ops/s: ${Utilities.oneSecInNano/meanRoverDurations}")

        println(s"NonRoverChat: mean time in nano $meanNonRoverDurations, std: ${Utilities.getStd(nonRoverDurations.slice(1, numRepetitions))}")
        println(s"    Ops/s: ${Utilities.oneSecInNano/meanNonRoverDurations}")

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

        println("\n\n")
        println("Benchmark Size overhead")
        microBench.getSizeOverhead
    }
}
