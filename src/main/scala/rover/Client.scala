package rover

import rover.rdo.AtomicObjectState

/**
  * Class encapsulating a Client, who interacts with a Server within a Session
  * for exchanging RDOs (in fact their atomic states).
  * @param serverAddress. the address of the corresponding server
  * @param identifier, the access token granted to the client for authorized access to server
  * @param mapToStates, map to up-to-date version of local RDOs
  */
//FIXME: create a unique, static id for each RDO upon its creation
class Client[C, A](private val serverAddress: String, private val identifier: Session[C, A]#Identifier,
                   private var mapToStates: Map[String, AtomicObjectState[A]]){

  val server = Server.fromAddress[C,A](serverAddress)
  def createSession(credentials: C): Session[C, A] = {
    server.createSession(credentials, this.identifier)
  }

  def appendedState(stateId: String, atomicState: AtomicObjectState[A]): Unit ={
    this.mapToStates = this.mapToStates + (stateId -> atomicState)
  }

  def getAtomicStateWithId(stateId: String): AtomicObjectState[A] ={
    return mapToStates(stateId)
  }
}


object Client {
  class OAuth2Credentials(val accessToken: String, val refreshToken: String) {}
  type OAuth2Client[A] = Client[OAuth2Credentials, A]

  def oauth2[A](): OAuth2Client[A] = {
    null
  }
}
