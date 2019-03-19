package votingapp

import rover.rdo.{AtomicObjectState, ConflictedState}
import rover.rdo.client.{CommonAncestor, RdObject}

class Poll(val question: String, val choices: List[PollChoice], state: AtomicObjectState[Votes]) extends RdObject[Votes](state) {

	def cast(vote: PollChoice): Unit = {
		modifyState(votes => votes.add(vote))
	}

	def result: PollResult = {
		val votesState = this.immutableState
		return new PollResult(votesState)
	}

	override def toString: String = immutableState.toString

//	override def currentVersion: Long = 0
//	override def stableVersion: Long = 0
}

object Poll {
	def apply(question: String, choices: List[PollChoice]): Poll = {
		return new Poll(question, choices, AtomicObjectState.initial(Votes(choices)))
	}

	def copyOf(poll: Poll): Poll = {
		return new Poll(poll.question, poll.choices, poll.state)
	}
}

class PollResult(private  val votes: Votes) {
	def winner: PollChoice = {
		votes.majorityChoice
	}
}

case class PollChoice(choice: String)

class Votes(val map: Map[PollChoice, Int]) {
	/**
	  * Adds the given poll choice to the votes cast. Also: immutable object pattern.
	  * @param vote The vote-choice to cast
	  * @return New state with the vote added
	  */
	def add(vote: PollChoice): Votes = {
		Votes(map updated (vote, map(vote) + 1))
	}

	def majorityChoice: PollChoice = {
		// FIXME: ties, idea: return a "poll-result" not the choice
		val winner = map.maxBy(_._2)._1
		return winner
	}

	override def toString: String = {
		map.toString
	}
}

object Votes {
	def apply(choices: List[PollChoice]): Votes= {
		// TODO: maybe remove, not needed anymore... we accept any choice? Or enforce only valid choices
		return new Votes(choices.map(choice => (choice,0)).toMap.withDefaultValue(0))
	}

	def apply(votes: Map[PollChoice, Int]): Votes = {
		return new Votes(votes.withDefaultValue(0))
	}
}

object henk {
	def main(args: Array[String]): Unit = {
		val poll = Poll("Does this work", List(PollChoice("Yes"), PollChoice("No"), PollChoice("I hope so"), PollChoice("Yes")))
		println(poll)

		val poll2 = Poll.copyOf(poll)
		println(poll2)

		println("\ncasting votes:")

		poll.cast(PollChoice("Yes"))
		poll.cast(PollChoice("Yes"))
		poll.cast(PollChoice("No"))

		poll2.cast(PollChoice("No"))
		poll2.cast(PollChoice("No"))
		println("Poll:" + poll)
		println("Poll2:" + poll2)

		val parent = CommonAncestor.from(poll, poll2)
		println("Parent:" + parent.toString)
		println(poll.result.winner)
		println("Immutable state:" + poll.toString)


	}
}
