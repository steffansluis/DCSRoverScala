package rover.rdo.client

import rover.rdo.AtomicObjectState

/**
  * The class Log encapsulates the records from all the active RDOs. The Log
  * contains a map of [Long, LogRecord] key-value pairs.
  */
//FIXME: Most probably should be moved to server side
class Log[A] {

  private var logMap: Map[Long, LogRecord[A]] = Map()

  def addToMap(key: Long, value: LogRecord[A]): Unit ={
    this.logMap = this.logMap + (key -> value)
  }

  def removeFromMap(key:Long): Unit ={
    if (logMap contains(key)){
      this.logMap = this.logMap - key
    }
  }

}

/**
  * This class encapsulates all the information stored to log regarding a single RDO
  * The class's fields are a list of atomic states, a list of immutable states and a
  * list of operations applied since the records instantiation.
  */
class LogRecord[A] extends OperationType[A]{
//  private var atomicStates: List[AtomicObjectState[A]] = List()
  private var immutableStates: List[A] = List()
  private var operations: List[Op] = List()

//  type Op = A => A

  def this(immutableStates: List[A]) {
    this()
//    this.atomicStates = atomicStates
    this.immutableStates = immutableStates
    this.operations = List()
  }


  def recordSize() = this.immutableStates.length

  def getImmutableStates : List[A] = this.immutableStates

  // Since the lists are immutable, there is no append but rather a new object
  def updateRecord(atomicState: AtomicObjectState[A], immutableState: A, operation: Op): Unit ={
//    this.atomicStates = this.atomicStates :+ atomicState
    this.immutableStates = this.immutableStates :+ immutableState
    this.operations = this.operations :+ operation
  }


  /**
    * This method empties the logRecord; at the flushing only the last atomic and immutable
    * states of the RDO are kept, which are also the current ones. On the contrary, operations
    * are completely flushed.
    */
  //FIXME: there is server-client interaction here; conflict resolution protocol should be incorp
  def flushRecord(): Unit ={
//    this.atomicStates = List[AtomicObjectState[A]](atomicStates.last)
    this.immutableStates = List[A](immutableStates.last)
    this.operations = List[Op]()
  }

}

trait OperationType[A]{
  type Op = A => A
}
