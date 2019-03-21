package rover

import rover.rdo.AtomicObjectState

//class Client[C](serverAddress: String, identifier: Session[C]#Identifier) {
//  val server = Server.fromAddress[C](serverAddress)
//
//  def createSession(credentials: C) = {
//    server.createSession(credentials)
//  }
//}

class Client[C, A](private val serverAddress: String, private val id: C, private var mapToStates: Map[String, AtomicObjectState[A]]){
  private val identifier: Session[C, A]#Identifier = null

  val server = Server.fromAddress[C,A](serverAddress)
  def createSession(credentials: C): Session[C, A] = {
    server.createSession(credentials)
  }

  def appended(stateId: String, atomicState: AtomicObjectState[A]): Unit ={
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
