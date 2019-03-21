package rover

import chatapp.{ChatMessage, User}
import rover.rdo.AtomicObjectState

class Server[C](identifier: Session[C]#Identifier) {

  // TODO: Determine what to do with this
  def clientFromCredentials(credentials: C): Client[C] = {
    null
  }

  def createSession(credentials: C) = {
    new Session[C](credentials, this, clientFromCredentials(credentials))
  }

  def notifyClient[T](client: Client[T], objectId: Session[T]#ObjectId) = {
    println(s"Notify client: $client with $objectId")
    client.invalidate(objectId)
  }
}

object Server {
  var CHAT_STATE: AtomicObjectState[Any] = AtomicObjectState.initial(List[Any]() :+ new ChatMessage("This is a test.", User.Giannis))
  val CHAT_SERVER = new Server[Any](null)
  def fromAddress[C](address: String): Server[C] = {
   CHAT_SERVER.asInstanceOf[Server[C]]
  }
}