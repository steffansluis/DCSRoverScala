package rover.rdo.conflict

import rover.rdo.state.{AtomicObjectState, RecordedStateModification}

class DiffWithAncestor[A <: Serializable](private val child: AtomicObjectState[A], private val ancestor: AtomicObjectState[A]) {

	lazy val asList: List[RecordedStateModification[A]] = determineDiff()
	
	private def determineDiff(): List[RecordedStateModification[A]] = {
		/**
		  *    children: O ---parent---> o ---parent---> o
		  *    ancestor:                                /|\
		  */
		for(i <- child.log.asList.reverse) {
			if(i.parent.contains(ancestor)) {
				// TODO: inefficient
				val indexOfI = child.log.asList.indexOf(i)
				val logRecordsUpToI = child.log.asList.slice(indexOfI, child.log.asList.size)
				
				return logRecordsUpToI
			}
		}

		throw new RuntimeException("Failed to determine difference with this ancestor")
	}

	override def toString: String = {
		"DiffWithAncestor{ \n	" + asList.mkString("\n	") + "\n}"
	}
}
