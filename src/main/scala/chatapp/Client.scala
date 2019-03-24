package chatapp

import chatapp.model.{Chat, ChatMessage}
import chatapp.ui.REPL
import rover.Client.OAuth2Credentials
import rover.rdo.RdObject
import rover.{HTTPClient, Session}

import scala.async.Async.{async, await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ChatClient(serverAddress: String) extends
	HTTPClient[List[ChatMessage]](serverAddress, (credentials) => credentials.accessToken) {
//	Client.OAuth2Client[List[ChatMessage]](serverAddress, (credentials: OAuth2Credentials) => credentials.accessToken, Map[String, AtomicObjectState[List[ChatMessage]]]()) {
	var session: Session[OAuth2Credentials, List[ChatMessage]] = null
	var user: ChatUser = null
	var chat: Chat = null;

	val printer = (string: String) => {
		val cls = s"${string.split("\n").map(c => s"${REPL.UP}${REPL.ERASE_LINE_BEFORE}${REPL.ERASE_LINE_AFTER}").mkString("")}"
		// Prepend two spaces to match input indentation of "> "
		val text = string.split("\n").map(line => s"  $line").mkString("\n")

		s"${REPL.SAVE_CURSOR}$cls\r$text${REPL.RESTORE_CURSOR}"
	}

		// TODO: This is hacky, figure out a better way to do this
	val updater: Chat#Updater = state => async {
		val text = state.immutableState.takeRight(ChatClient.SIZE).map(m => m.toString()).mkString("\n")
		print(s"${printer(text)}")
	}

	def login(user: ChatUser): Future[Unit] = {
		async {
			val credentials = new OAuth2Credentials("fake credentials",  "fake credentials")
			this.user = user
			session = createSession(credentials)
			val state = importRDO("chat")
			val rdo = new RdObject[List[ChatMessage]](state)
			chat = Chat.fromRDO(rdo)
//			println(s"Initial state: ${chat.state}")
		}
	}

		def send(message: String): Future[Unit] = {
//		println(s"Sending message with intial state: ${chat.state}")
		async {
			await(chat.send(new ChatMessage(message, user)))
			exportRDO("chat", chat.state)
			//			await(session.exportRDO("chat", chat))
//			updater(chat.state)
		}
	}

	def updateLoop(): Future[Unit] = {
		async {
			while(true) {
//				val serverState = await(importRDO("chat")).asInstanceOf[RdObject[List[ChatMessage]]]
//				chat = Chat.fromRDO(serverState, updater)
//				println(s"Updating state from update loop...")
				val state = importRDO("chat")
//				println(s"Got updated state: $state")
				//				chat = Chat.fromRDO(rdo, updater)

				appendedState("chat", state)
				chat.state = state

//				println(s"Rendering chat state: ${chat.state}")
				updater(chat.state) // Force re-render
				Thread.sleep(ChatClient.UPDATE_DELAY_MS)
			}
		}
	}

	def render(): Future[Unit] = {
//		val chat = new Chat(updater)
		println(s"  Welcome to Rover Chat! Connected to: $serverAddress")
		print((1 to ChatClient.SIZE).map(i => "\n").mkString(""))

		val reader = () => {
			print("> ")
			val s = scala.io.StdIn.readLine()
			s
		}
		val executor = (input: String) => {
		async {
			val p = send(input)
			await(p)
				// This clears the input line
				print(s"${REPL.UP}${REPL.ERASE_LINE_AFTER}")
				chat.state.immutableState.takeRight(ChatClient.SIZE).map(m => s"${m.toString()}").mkString("\n")
			}
		}
		val repl: REPL[String] = new REPL(reader, executor, printer)
//		Await.result(repl.loop(), Duration.Inf)
		updateLoop() // TODO: Memory leak
		repl.loop()
	}

}

object ChatClient {
	val SIZE = 10
	val UPDATE_DELAY_MS = 2000

	def main(args: Array[String]): Unit = {
		val serverAddress = "localhost"
		val client = new ChatClient(serverAddress)
		val f = async {
			await(client.login(ChatUser.Steffan))
			await(client.render())
		}

		Await.result(f, Duration.Inf)
	}
}

object Bot {
		def main(args: Array[String]): Unit = {
		val serverAddress = "localhost"
		val client = new ChatClient(serverAddress)
		val f = async {
			await(client.login(ChatUser.Giannis))
			client.updateLoop() // TODO: Memory leak

		// Simulate conversation
			Thread.sleep(3000)
			await(client.send("Hey man!"))

			Thread.sleep(3000)
			await(client.send("How's it going?"))

			Thread.sleep(10000)
			await(client.send("Yea man I'm good"))
		}

		Await.result(f, Duration.Inf)
	}
}