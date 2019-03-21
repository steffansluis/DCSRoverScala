package rover

import rover.rdo.AtomicObjectState
import rover.rdo.client.{CommonAncestor, RdObject}


class Server[C, A](private val address: String, private val mapToClients: Map[Session[C, A]#Identifier, Client[C, A]],
                   private var mapToStates: Map[String, AtomicObjectState[A]]) {

  //FIXME: Does the server has its own creds? It merely keeps track of clients' creds
//  val credentials = null

  // TODO: Determine what to do with this
  def clientFromCredentials(credentials: Session[C,A]#Identifier): Client[C, A] = {
    new Client[C, A](this.address, credentials, Map[String, AtomicObjectState[A]]())
  }

  def createSession(credentials: C, identifier: Session[C,A]#Identifier): Session[C, A] = {
    new Session[C, A](credentials, this, clientFromCredentials(identifier))
  }

  def getAtomicStateWithId(stateId: String): AtomicObjectState[A] = {
    return mapToStates(stateId)
  }

  def deliveredState(stateId: String, atomicState: AtomicObjectState[A]): Unit ={
    this.mapToStates = this.mapToStates + (stateId -> atomicState)
  }

  def receivedState(stateId: String, state: AtomicObjectState[A]): Unit ={
    val clientRDO = new RdObject[A](state)
    val serverRDO = new RdObject[A](mapToStates(stateId))
    val ancestor = new CommonAncestor[A](serverRDO, clientRDO)
    if (ancestor == serverRDO) deliveredState(stateId, state)
    else {
      //FiXME: Conflict resolution and history diff stuff
    }
  }
}

object Server {
  def fromAddress[C, A](address: String): Server[C, A] = {
    return new Server[C, A](address, Map[Session[C, A]#Identifier, Client[C,A]](), Map[String, AtomicObjectState[A]]())
  }

  def getMapOfServer[C, A](server: Server[C, A]): Map[String, AtomicObjectState[A]] ={
    return server.mapToStates
  }
}