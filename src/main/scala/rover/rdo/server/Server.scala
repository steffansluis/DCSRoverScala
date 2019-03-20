package rover.rdo.server

import java.util
import java.net.{HttpURLConnection, ServerSocket, Socket, URL}

import rover.rdo.{AtomicObjectState, ConflictedState}
import com.google.gson.Gson
import javax.ws.rs.core.Response
import javax.ws.rs.{GET, Path}
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.{DefaultHttpClient, HttpClients}
import org.apache.http.message.BasicNameValuePair
import votingapp.{Poll, PollChoice, Votes}



trait RoverServer[A]{
    def importRdOwithState(stateId: String, incomingState: AtomicObjectState[A]): ConflictedState[A]
    def exportRdOwithState(stateId: String)

    def updateState(stateId: String)
}

class Server[A](private val mapToStates: Map[String, AtomicObjectState[A]],
                           private val mapToUsers: Map[String, User]) extends RoverServer[A]{


    override def importRdOwithState(stateId: String, incomingState: AtomicObjectState[A]): ConflictedState[A] = {
        val serverState = mapToStates(stateId)
        if (serverState != incomingState) {
            return new ConflictedState[A](serverState, incomingState)
        }
        throw new RuntimeException("Matching states")
    }

    //noinspection ScalaDeprecation
    override def exportRdOwithState(stateId: String): Unit = {
        val stateToBeExported = mapToStates(stateId)
        val exportToUser = mapToUsers(stateId)
        val stateAsJson = new Gson().toJson(stateToBeExported)

        val post = new HttpPost("http://localhost:8080/posttest")
        val nameValuePairs = new util.ArrayList[BasicNameValuePair]()
        nameValuePairs.add(new BasicNameValuePair("JSON", stateAsJson))
        post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf-8"))

        // send the post request
//        val clientSocket = new Socket("localhost", 8080)
        val httpClient = HttpClients.createDefault()
        val httpResponse = httpClient.execute(post)
        println("--- HEADERS ---")
        httpResponse.getAllHeaders.foreach(arg => println(arg))
    }

    override def updateState(stateId: String): Unit = ???

}

case class User(id: String, hostAddress: String)

object Server{
    var socket: ServerSocket = null
    def init: Unit ={
        this.socket = new ServerSocket(8080)
        socket.accept()
    }
    def isAlive[A]: Boolean ={
        this.socket == null

    }

}

import rover.rdo.server.Server
@Path("server")
class ServerEndpoint[A] {

    @Path("health")
    @GET
    def checkHealth(): Response ={
        if(Server.isAlive){
            return Response.noContent().build()
        }
        else{
            return Response.serverError().build()
        }
    }
}

object henk{
    def main(args: Array[String]): Unit ={
//        val poll = Poll("Does this work", List(PollChoice("Yes"), PollChoice("No"), PollChoice("I hope so"), PollChoice("Yes")))
//        val users = new User("123", "foo")
//        val Server = new Server[Votes](Map("123"-> poll.state), Map("123" -> users))
//        Server.init
//        Server.exportRdOwithState("123")
        Server.init
        while (true){

        }


    }

}