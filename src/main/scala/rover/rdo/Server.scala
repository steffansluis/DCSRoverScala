package rover.rdo

import rover.rdo.client.{AtomicRDObject, Client}

class Server[S <: AtomicRDObject[S]] {

  // TODO: Implement this
  def checkedOutStates: Map[Client, S] = {
    return null
  }

}
