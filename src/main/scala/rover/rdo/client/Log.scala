package rover.rdo.client

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
    if (logMap contains key){
      this.logMap = this.logMap - key
    }
  }

}

/**
  * This class encapsulates all the information stored to log regarding a single RDO
  * The class's fields are a list of immutable states and a
  * list of operations applied since the records instantiation.
  */
class LogRecord[A]{
  private var immutableStates: List[A] = List()
  private var operations: List[Op] = List()

  type Op = A => A

  def this(immutableStates: List[A]) {
    this()
    this.immutableStates = immutableStates
    this.operations = List()
  }


  def recordSize(): Int = this.immutableStates.length

  def getImmutableStates : List[A] = this.immutableStates

  // Since the lists are immutable, there is no append but rather a new object
  def updateRecord(immutableState: A, operation: Op): Unit ={
    this.immutableStates = this.immutableStates :+ immutableState
    this.operations = this.operations :+ operation
  }


  /**
    * This method empties the logRecord; at the flushing only the last immutable
    * states of the RDO are kept, which are also the current ones. On the contrary, operations
    * are completely flushed.
    */
  //FIXME: there is server-client interaction here; conflict resolution protocol should be incorp
  def flushRecord(): Unit ={
    this.immutableStates = List[A](immutableStates.last)
    this.operations = List[Op]()
  }

}
