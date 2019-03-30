package rover.rdo.conflict

import rover.rdo.state.{AtomicObjectState, RecordedStateModification}

class DiffWithAncestor[A <: Serializable](private val child: AtomicObjectState[A], private val ancestor: AtomicObjectState[A]) {

	def asList: List[RecordedStateModification[A]] = {
		for(i <- child.log.asList) {
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
