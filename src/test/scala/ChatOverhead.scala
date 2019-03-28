import chatapp.{Chat, ChatMessage, ChatUser}
import rover.rdo.client.RdObject
import rover.rdo.state.AtomicObjectState

import scala.util.Random

class NonRoverChat(var state: List[ChatMessage]) {

    def send(message: ChatMessage): Unit = {
        this.state = this.state :+ message
    }

    override def toString: String = {
        return this.state.last.toString
    }

    def mkString: String = {
        return this.state.mkString("\n")
    }
}

object NonRoverChat {
    def initial(initialState: List[ChatMessage]): NonRoverChat = {
        return new NonRoverChat(initialState)
    }

}

object Statistics {

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

class ChatOverheadMicroBench(val numRepetitions: Int,
                             val maxMessageLength: Int,
                             var roverBenchDurations: List[Long] = List[Long](),
                             var nonRoverBenchDurations: List[Long] = List[Long]()) {


    val nonRoverChat: NonRoverChat = NonRoverChat.initial(List[ChatMessage]())
    val roverChat: Chat = Chat.fromRDO(new RdObject[List[ChatMessage]](AtomicObjectState.initial(List[ChatMessage]())))

    def generateRandomMessages(): List[ChatMessage] = {
        var messageLength : Int = 0
        var messageBody: String = null
        var randomMessages = List[ChatMessage]()

        for (i <- Range.inclusive(1, numRepetitions)){
            messageLength = Random.nextInt(maxMessageLength)
            messageBody = Random.alphanumeric.take(messageLength).mkString
            randomMessages = randomMessages :+ new ChatMessage(messageBody, ChatUser.System)
        }
        return randomMessages
    }

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
        val randomMessages = generateRandomMessages()
        val roverDurations = benchmarkRoverChat(randomMessages)
        val nonRoverDurations = benchmarkNonRoverChat(randomMessages)

        val meanRoverDurations = Statistics.getMean(roverDurations)
        val meanNonRoverDurations = Statistics.getMean(nonRoverDurations)
        println(s"RoverChat: mean time in nano: $meanRoverDurations, std: ${Statistics.getStd(roverDurations)}")
        println(s"NonRoverChat: mean time in nano $meanNonRoverDurations, std: ${Statistics.getStd(nonRoverDurations)}")

        println("\n")
        println(f"Overhead: ${math.abs(meanRoverDurations.asInstanceOf[Double] - meanNonRoverDurations.asInstanceOf[Double])/ meanNonRoverDurations.asInstanceOf[Double] }%1.4f")

    }
}

object ChatOverheadMicroBench {
    def main(args: Array[String]): Unit = {

        val microBench = new ChatOverheadMicroBench(10, 100)
        microBench.run

    }
}
