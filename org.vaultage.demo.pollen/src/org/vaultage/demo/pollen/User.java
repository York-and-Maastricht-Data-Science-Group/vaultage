
package org.vaultage.demo.pollen;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class User extends UserBase {
	private String name = new String();
	private Map<String, NumberPoll> polls = new HashMap<>();
	private Map<String, Double> pollFakeValues = new HashMap<>();
	private Map<String, PollAnswer> pollAnswers = new HashMap<>();
	public long waitTime = 0;
	public int count = 0;

	private OnPollReceivedListener onPollReceivedListener;

	public User() throws Exception {
		super();
	}

	public double getPollFakeValue(String pollId) {
		return pollFakeValues.get(pollId);
	}



	public void addPollFakeValue(String pollId, double value) {
		pollFakeValues.put(pollId, value);
	}

	// getter
	public String getName() {
		return this.name;
	}

	// setter
	public void setName(String name) {
		this.name = name;
	}

//	// operations
//	public double sendNumberPoll(String requesterPublicKey, NumberPoll poll) throws Exception {
//		return 1.0;
//	}

	// operations
	public double sendNumberPoll(User requesterUser, NumberPoll poll) throws Exception {
		double result = 0;
		double answer = 0;

		if (this.getPublicKey().equals(poll.getOriginator())) {
			double fakeSalary = (new Random()).nextInt(200 - 100) + 100;
			pollFakeValues.put(poll.getId(), fakeSalary);
			result = fakeSalary;
		} else {
			Iterator<String> iterator = poll.getParticipants().iterator();
			while (iterator.hasNext()) {
				String item = iterator.next();
				if (item.equals(this.getPublicKey()) || item.equals(requesterUser.getPublicKey())) {
					iterator.remove();
				}
			}
			PollAnswer pollAnswer = new PollAnswer(poll);
			pollAnswers.put(poll.getId(), pollAnswer);
			Thread t = new Thread() {
				@Override
				public void run() {
					User.this.getOnPollReceivedListener().onPollReceived(User.this, poll);
				}
			};
			t.setName(poll.getId());
			t.start();
			synchronized (pollAnswer) {
				pollAnswer.wait();
			}
			answer = pollAnswer.getAnswer();

			if (poll.getParticipants().size() == 0) {
				(new RemoteUser(User.this, poll.getOriginator())).sendNumberPoll(poll);
			} else {
				(new RemoteUser(this, poll.getParticipants().get(0))).sendNumberPoll(poll);
			}
			synchronized (this.getSendNumberPollResponseHandler()) {
//				System.out.println(this.getName() + " starts waiting ...");
				this.getSendNumberPollResponseHandler().wait();
				result = answer + getSendNumberPollResponseHandler().getResult();
//				System.out.println(this.getName() + " ends waiting");
			}
		}
		return result;
	}

	public List<Integer> sendMultivaluedPoll(User requesterUser, MultivaluedPoll poll) throws Exception {
		throw new Exception();
	}

	public Map<String, NumberPoll> getPolls() {
		return polls;
	}

	public void setPolls(Map<String, NumberPoll> polls) {
		this.polls = polls;
	}

	public double sendNumberPoll(String from, NumberPoll poll, int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	public OnPollReceivedListener getOnPollReceivedListener() {
		return onPollReceivedListener;
	}

	public void setOnPollReceivedListener(OnPollReceivedListener onPollReceivedListener) {
		this.onPollReceivedListener = onPollReceivedListener;
	}

	public PollAnswer getPollAnswer(String pollId) {
		return pollAnswers.get(pollId);
	}

}