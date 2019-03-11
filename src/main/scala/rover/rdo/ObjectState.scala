package rover.rdo

import votingapp.{PollChoice, Votes}

trait ObjectState {

}

class AtomicObjectState extends ObjectState {
	// not sure due to types... do we want the _specific_ subtype here? Not the abstract state
	private var ops: List[Op[AtomicObjectState]] = List()

	protected def apply(operation: Op[AtomicObjectState]) = {
		operation.applyOn(this)
		this.ops = this.ops :+ operation
	}
}

// framework
trait Op[ObjectState] {
	def applyOn(state: ObjectState): ObjectState
}

// app code
class CastVoteOp(private val vote: PollChoice) extends Op[Votes] {
	override def applyOn(state: Votes): Votes = {
		return state.add(vote)
	}
}