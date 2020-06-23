package org.vaultage.demo.pollen;

public class PollAnswer {

	private NumberPoll poll;
	private double answer = 0;

	public PollAnswer(NumberPoll poll) {
		this.setPoll(poll);
	}

	public void submitAnswer(double answer) {
		this.answer = answer;
		synchronized (this) {
			this.notify();
		}
	}

	public NumberPoll getPoll() {
		return poll;
	}

	public void setPoll(NumberPoll poll) {
		this.poll = poll;
	}

	public double getAnswer() {
		return answer;
	}
}
