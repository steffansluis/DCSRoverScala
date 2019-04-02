package chatapp

import rover.rdo.CommonAncestor
import rover.rdo.client.RdObject
import rover.rdo.conflict.ConflictedState
import rover.rdo.state.{AtomicObjectState, InitialAtomicObjectState}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.async.Async.{async, await}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.Random


// FIXME: ensure messages can be read, but not modified or reassigned...
// FIXME: after state & rd object impl change
class Chat(_initialState: AtomicObjectState[List[ChatMessage]]) extends RdObject[List[ChatMessage]](_initialState) with Serializable {
//	type State = List[ChatMessage]
	type Updater = AtomicObjectState[List[ChatMessage]] => Future[Unit]

	val _onStateModified: Chat#Updater = null

	def send(message: ChatMessage): Future[Unit]= {
		val op: AtomicObjectState[List[ChatMessage]]#Op = s => s :+ message

		async {
			modifyState(op)
		}
	}

	def sendSynchronous(message: ChatMessage): Unit = {
		val op: AtomicObjectState[List[ChatMessage]]#Op = s => s :+ message
		modifyState(op)
	}


	override def onStateModified(oldState: AtomicObjectState[List[ChatMessage]]): Future[Unit] = {
		_onStateModified(state)
	}

	def currentVersion(): Long = {
		immutableState.size
	}

}

object Chat {
	def fromRDO(rdo: RdObject[List[ChatMessage]]): Chat = {
		new Chat(rdo.state)
	}

	def copyOf(chat: Chat) = {
		new Chat(chat.state)
	}

	def generateRandomMessages(numMessages: Int, maxMessageLength: Int): List[ChatMessage] = {
		var messageLength : Int = 0
		var messageBody: String = null
		var randomMessages = List[ChatMessage]()

		Range.inclusive(1, numMessages).foreach(_ => {
			messageLength = Random.nextInt(maxMessageLength)
			messageBody = Random.alphanumeric.take(messageLength).mkString
			randomMessages = randomMessages :+ new ChatMessage(messageBody, ChatUser.System)
		})
		return randomMessages
	}
}


class NonRoverChat(var state: List[ChatMessage]) extends Serializable {

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




