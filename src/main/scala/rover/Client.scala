package rover

class Client[C](serverAddress: String, identifier: Session[C]#Identifier) {
  val server = Server.fromAddress[C](serverAddress)

  def createSession(credentials: C) = {
    server.createSession(credentials)
  }

  def invalidate[T](objectId: Session[T]#ObjectId) = {
    println(s"Invalidated $objectId")
  }
}


object Client {
  class OAuth2Credentials(val accessToken: String, val refreshToken: String) {}
  type OAuth2Client = Client[OAuth2Credentials]

  def oauth2(): OAuth2Client = {
    null
  }
}
