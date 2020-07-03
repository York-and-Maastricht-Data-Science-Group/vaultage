package org.vaultage.demo.pollen;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class User extends UserBase {

	public static final int MIN_RANDOM = 1000000;
	public static final int MAX_RANDOM = 5000000;

	private String name = new String();

	// The following maps use message tokens as string to recover the information 
	//   later during the requests/responses interchange

	/** Initiated polls by the messageToken returned by the RemoteVault */
	private Map<String, NumberPoll> initiatedPolls = new HashMap<>();

	/** Pending poll requests by the token of the received request */
	private Map<String, NumberPoll> pendingPollRequests = new HashMap<>();

	/**
	 * Map from the response token of a relayed request (to the next participant)
	 * to the request token of the previously received request (from the previous participant)
	 */
	private Map<String, String> responseToRequestMapper = new HashMap<>();

	// The following maps use the poll id to store the fake value and the final answer
	private Map<String, Double> numberPollFakeValues = new HashMap<>();
	private Map<String, Double> numberPollAnswers = new HashMap<>();

	private OnPollReceivedListener onPollReceivedListener;

	public User() throws Exception {
		super();
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
	public void sendNumberPoll(User requesterUser, String requestToken, NumberPoll poll) throws Exception {

		if (publicKey.equals(poll.getOriginator())) {
			// this vault is the poll's originator, respond with fake value
			double fakeValue = (new Random()).nextInt(MAX_RANDOM - MIN_RANDOM) + MIN_RANDOM;
			numberPollFakeValues.put(poll.getId(), fakeValue);
			RemoteUser remote = new RemoteUser(this, requesterUser.getPublicKey());
			remote.respondToSendNumberPoll(fakeValue, requestToken);
		} else {
			int index = poll.getParticipants().indexOf(publicKey);

			String nextParticipant;
			if (index + 1 == poll.getParticipants().size()) {
				// last participant, send request back to originator
				nextParticipant = poll.getOriginator();
			}
			else {
				// send request to the next participant
				nextParticipant = poll.getParticipants().get(index + 1);
			}
			// pending poll requests information is stored in two different places:
			//   pendingPollRequests: stores the poll based on the requestToken
			//   responseToRequestMapper: when asking the next participant for
			//     their poll answer, a different token is used (mainly to avoid
			//     that the whole poll shares the same token, which might be
			//     dangerous in terms of security). The mappper stores the link
			//     between the request received from the previous participant
			//     and the awaiting response from the next
			pendingPollRequests.put(requestToken, poll);

			synchronized(responseToRequestMapper) {
				RemoteUser remote = new RemoteUser(this, nextParticipant);
				String responseToken = remote.sendNumberPoll(poll);
				responseToRequestMapper.put(responseToken, requestToken);
			}
		}
	}

	public void sendMultivaluedPoll(User requesterUser, String requestToken, MultivaluedPoll poll) throws Exception {
		throw new Exception();
	}

	public void addInitiatedNumberPoll(NumberPoll poll, String messageToken) {
		initiatedPolls.put(messageToken, poll);
	}

	public NumberPoll getInitiatedNumberPoll(String messageToken) {
		return initiatedPolls.get(messageToken);
	}

	/**
	 * Get a received poll request that is awaiting for a response
	 * 
	 * @param responseToken The token previously used to request an answer to
	 *                      the next participant of the poll
	 */
	public NumberPoll getPendingNumberPollByResponseToken(String responseToken) {
		synchronized (responseToRequestMapper) {
			return pendingPollRequests.get(responseToRequestMapper.get(responseToken));
		}
	}

	public String getMappedRequestToken(String responseToken) {
		return responseToRequestMapper.get(responseToken);
	}

	public void addNumberPollFakeValue(String messageToken, double value) {
		numberPollFakeValues.put(messageToken, value);
	}

	public double getNumberPollFakeValue(String messageToken) {
		return numberPollFakeValues.get(messageToken);
	}

	public void addNumberPollAnswer(String messageToken, double value) {
		numberPollAnswers.put(messageToken, value);
	}

	public double getNumberPollAnswer(String messageToken) {
		return numberPollAnswers.get(messageToken);
	}

	public OnPollReceivedListener getOnPollReceivedListener() {
		return onPollReceivedListener;
	}

	public void setOnPollReceivedListener(OnPollReceivedListener onPollReceivedListener) {
		this.onPollReceivedListener = onPollReceivedListener;
	}
}