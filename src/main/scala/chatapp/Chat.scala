package chatapp

import rover.rdo.AtomicObjectState
import rover.rdo.client.RdObject

// FIXME: ensure messages can be read, but not modified or reassigned...
// FIXME: after state & rd object impl change
class Chat(var messages: List[ChatMessage]) extends RdObject[String](AtomicObjectState.initial("")) {

	// TODO: Something with users, crypto stuff on identity
	// FIXME: Hashes should be used here as well as user ids
	private var users = Map[Long, String]()

	def this(messages: List[ChatMessage], users: Map[Long, String]) {
		this(messages)
		this.users = users
	}

	def addUser(id: Long, uri: String): Unit = {
		if (!this.users.contains(id)) this.users updated (id, uri)
		// FIXME: otherwise we can also update, a user's uri
		else println("Already existing user")
	}

	def removeUser(id : Long)= {
		if(users.contains(id)) users = users - id
		else println("Non-exsiting id given")
	}

	def appendMessage(chatMessage: ChatMessage): Unit = {
		messages = messages :+ chatMessage
	}

	def terminate(): Unit ={
		//TODO: terminate
	}

	def currentVersion(): Long = {
		messages.size
	}

//	override def stableVersion: Long = {
//		// TODO: the stable version (last committed)
//		messages.size
//	}

	def printMessages(): Unit = {
		for (i <- messages) {
			println(s"body: ${i.body}, author: ${i.author}, time: ${i.timestamp}")
		}
	}
}

class ChatMessage(val body: String,
                  val author: String,
                  val timestamp: Long = java.time.Instant.now.getEpochSecond())
{

}

object chatFoo{
	def main(args: Array[String]): Unit ={
		val chat = new Chat(messages = List[ChatMessage]())
		chat.addUser(1234, "tt")
		val m: ChatMessage = new ChatMessage("test message", "Ioa")
		chat.appendMessage(m)
		chat.printMessages()
	}
}
