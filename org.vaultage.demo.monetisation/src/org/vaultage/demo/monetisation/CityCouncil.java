package org.vaultage.demo.monetisation;

import java.util.ArrayList;
import java.util.List;

public class CityCouncil extends CityCouncilBase {

	private List<Poll> polls = new ArrayList<Poll>();
	
	public CityCouncil() throws Exception {
		super();
	}
	
	public CityCouncil(String address, int port) throws Exception {
		super(address, port);
	}
	
	public void getPoll(String requesterPublicKey, String requestToken, String pollId) throws Exception {
		RemoteCityCouncil confirmer = new RemoteCityCouncil(this, requesterPublicKey);
		Poll poll = polls.stream().filter(p -> p.getId().equals(pollId)).findFirst().orElse(null);
		confirmer.respondToGetPoll(poll, requestToken);
	}
	
	public void submitPoll(String requesterPublicKey, String requestToken, Poll poll) throws Exception {
		RemoteCityCouncil confirmer = new RemoteCityCouncil(this, requesterPublicKey);
		confirmer.respondToSubmitPoll("OK", requestToken);
	}

	public List<Poll> getPolls() {
		return polls;
	}

	public void setPolls(List<Poll> polls) {
		this.polls = polls;
	}
	
		
}