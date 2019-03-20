package rover.rdo.client

import rover.rdo.AtomicObjectState

trait RoverClient[A] {
    def importRdOwithState(stateId: String)
    def exportRdOwithState(stateId: String, state: AtomicObjectState[A])
}

class Client[A](private val id: String, private val mapToStates: Map[String, AtomicObjectState[A]],
                private val mapToServers: Map[String, String]) {

    def init(): Unit ={

    }


}
