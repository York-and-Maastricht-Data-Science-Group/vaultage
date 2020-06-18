/**** protected region User on begin ****/
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
	private Map<String, Double> pollRealValues = new HashMap<>();
	public long waitTime = 0;

	public User() throws Exception {
		super();
	}

	public double getPollFakeValue(String pollId) {
		return pollFakeValues.get(pollId);
	}
	
	public double getPollRealValue(String pollId) {
		return pollRealValues.get(pollId);
	}
	
	public void addPollFakeValue(String pollId, double value) {
		pollFakeValues.put(pollId, value);
	}
	
	public void addPollRealValue(String pollId, double value) {
		pollRealValues.put(pollId, value);
	}

	// getter
	public String getName() {
		return this.name;
	}

	// setter
	public void setName(String name) {
		this.name = name;
	}

	// operations
	public double sendNumberPoll(String requesterPublicKey, NumberPoll poll) throws Exception {
		double result = 0;
		this.polls.put(poll.getId(), poll);
		if (this.getPublicKey().equals(poll.getOriginator())) {
			double fakeSalary = (new Random()).nextInt(200 - 100) + 100;
			pollFakeValues.put(poll.getId(), fakeSalary);
			result = fakeSalary;
		} else {
			Iterator<String> iterator = poll.getParticipants().iterator();
			while(iterator.hasNext()) {
				String item = iterator.next();
				if (item.equals(this.getPublicKey()) || item.equals(requesterPublicKey)) {
					iterator.remove();
				}
			}
			if (poll.getParticipants().size() == 0) {
				result = result + this.getRemoteRequester().sendNumberPoll(poll.getOriginator(), poll);
			} else {
				result = result + this.getRemoteRequester().sendNumberPoll(poll.getParticipants().get(0), poll);
			}
			
//			for (String publicKey : poll.getParticipants()) {
//				if (!publicKey.equals(this.getPublicKey()) && !publicKey.equals(requesterPublicKey)) {
//					result = result + this.getRemoteRequester().sendNumberPoll(publicKey, poll);
//			//		this.getRemoteRequester().requestSendNumberPoll(publicKey, poll);
//				}
//			}
		}
		return result;
	}

	public List<Integer> sendMultivaluedPoll(String requesterPublicKey, MultivaluedPoll poll) throws Exception {
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

}
/**** protected region User end ****/