package rover

import rover.rdo.AtomicObjectState
import rover.rdo.client.{CommonAncestor, RdObject}

class Server[C, A](identifier: Session[C, A]#Identifier, private val address: String,
                   private val mapToClients: Map[C, Client[C, A]], private var mapToStates: Map[String, AtomicObjectState[A]]) {
  // TODO: Determine what to do with this
  def clientFromCredentials(credentials: C): Client[C, A] = {
    new Client[C, A](this.address, credentials, Map[String, AtomicObjectState[A]]())
  }

  def createSession(credentials: C): Session[C, A] = {
    new Session[C, A](credentials, this, clientFromCredentials(credentials))
  }

  def deliveredState(stateId: String, atomicState: AtomicObjectState[A]): Unit ={
    this.mapToStates = this.mapToStates + (stateId -> atomicState)
  }

  def getAtomicStateWithId(stateId: String): AtomicObjectState[A] = {
    return mapToStates(stateId)
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
    return new Server[C, A](null, address, Map[C, Client[C,A]](), Map[String, AtomicObjectState[A]]())
  }
}