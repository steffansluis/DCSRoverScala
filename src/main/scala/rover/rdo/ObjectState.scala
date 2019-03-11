
package rover.rdo {
	import scala.Predef

	trait ObjectState {

	}

	abstract class AtomicObjectState[T <: Op[_ <: AtomicObjectState[T]]] extends ObjectState {
		// not sure due to types... do we want the _specific_ subtype here? Not the abstract state
		private var ops: List[T] = List()

		protected def applyOp(operation: T): Unit = {
			// Let the operation apply itself on the state
//			operation.
			operation.applyOn(this)

			// Record the operation in the "log" or "event stream" of operations
			this.ops = this.ops :+ operation
		}

	}

	// framework
	abstract class Op[T <: AtomicObjectState[_ <: Op[T]]] {
		/**
		  * <p>Applies the operation on the state, must return the new, modified state</p>
		  * <p><b>WARNING: DO NOT USE THIS DIRECTLY!
		  *     Only AtomicObjectState should use this method.
		  *     Couldn't lock this down properly, perhaps some day
		  * </b></p>
		  * @param state
		  * @return
		  */
		private[rdo] def applyOn(state: T): T
	}
}