package rover.rdo
//{
	
	trait ObjectState {
	
	}
	
	abstract class AtomicObjectState[A <: AtomicObjectState[A]] extends ObjectState {
		// binding this to a self-type A
		this: A =>
		
		// not sure due to types... do we want the _specific_ subtype here? Not the abstract state
		private var ops: List[Op] = List()
		
		type Op = A => A
//		type Op = AtomicObjectState => AtomicObjectState
		
		def applyOp(operation: Op): A = {
			// Let the operation apply itself on the state
			//			operation.
			val result = operation.apply(this)

			// Record the operation in the "log" or "event stream" of operations
			this.ops = this.ops :+ operation

			return result
		}
	}
//}
	

	
//	abstract class Op[AbstractObjectState] {
//		/**
//		  * <p>Applies the operation on the state, must return the new, modified state
//		  *
//		  * <p><p>
//		  * <b>WARNING: DO NOT USE THIS DIRECTLY!
//		  *     Only AtomicObjectState should use this method.
//		  * </b>
//		  * @param state
//		  * @return
//		  */
//		private[rdo] def applyOn(state: T): T
//	}

// framework
//abstract class Op[T <: AtomicObjectState] {
//	/**
//	  * <p>Applies the operation on the state, must return the new, modified state
//	  *
//	  * <p><p>
//	  * <b>WARNING: DO NOT USE THIS DIRECTLY!
//	  *     Only AtomicObjectState should use this method.
//	  * </b>
//	  * @param state
//	  * @return
//	  */
//	private[rdo] def applyOn(state: T): T
//}

//
//	abstract class
//}