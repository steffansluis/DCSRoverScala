package rover

class Server[C](identifier: Session[C]#Identifier) {
  // TODO: Determine what to do with this
  def clientFromCredentials(credentials: C): Client[C] = {
    null
  }

  def createSession(credentials: C) = {
    new Session[C](credentials, this, clientFromCredentials(credentials))
  }
}

object Server {
  def fromAddress[C](address: String): Server[C] = {
    new Server[C](null)
  }
}