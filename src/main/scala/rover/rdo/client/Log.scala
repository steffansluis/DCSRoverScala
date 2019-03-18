package rover.rdo.client

import rover.rdo.AtomicObjectState

case class LogRecord[A](stateFrom: A, op: AtomicObjectState[A]#Op, stateResult: A) {
}

/**
  * This class encapsulates all the information stored to log regarding a single RDO state
  */
class Log[A](private val logList: List[LogRecord[A]] = List()) {

  // Since the lists are immutable, there is no append but rather a new object
  def appended(logRecord: LogRecord[A]): Log[A] = {
    val list = this.logList :+ logRecord
    return new Log[A](list)
  }

  def asList: List[LogRecord[A]] = {
    return logList
  }
}

object Log {
  /**
    * Constructs a new log with an initial state. Use for objects with fresh state
    * @tparam A
    * @return
    */
    def withInitialState[A](a: A): Log[A] = {
        return new Log[A]().appended(LogRecord[A](a,null, a))
    }
}
