package org.vaultage.demo.pollen.custom;

import java.util.List;

import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageMessage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.pollen.SendMultivaluedPollRequestHandler;
import org.vaultage.demo.pollen.SendNumberPollRequestHandler;
import org.vaultage.demo.pollen.User;
import org.vaultage.demo.pollen.MultivaluedPoll;
import org.vaultage.demo.pollen.NumberPoll;

public class CustomRemoteUser {

	protected VaultageServer server;
	protected User requesterVault;
	protected String remoteUserPublicKey;
	
	public CustomRemoteUser(VaultageServer server, User vault, String remoteUserPublicKey) throws Exception {
		this.server = server;
		this.requesterVault = vault;
		this.remoteUserPublicKey = remoteUserPublicKey;
	}
	
	public double sendNumberPoll(NumberPoll poll) throws Exception {
	
		VaultageMessage message = new VaultageMessage();
		message.setSenderId(requesterVault.getId());
		message.setFrom(requesterVault.getPublicKey());
		message.setTo(remoteUserPublicKey);
		message.setOperation(SendNumberPollRequestHandler.class.getName());
		
		message.putValue("poll", Vaultage.Gson.toJson(poll));
		
		this.requesterVault.getSendNumberPollResponseBaseHandler().setCallerThread(Thread.currentThread());
		
		this.requesterVault.getVaultage().sendMessage(message.getTo(), requesterVault.getPublicKey(),
		requesterVault.getPrivateKey(), message);
		
		synchronized (Thread.currentThread()) {
			Thread.currentThread().wait();
		}
		
		return (double) requesterVault.getSendNumberPollResponseBaseHandler().getResult();
		
	}
	
	public List<Integer> sendMultivaluedPoll(MultivaluedPoll poll) throws Exception {
	
		VaultageMessage message = new VaultageMessage();
		message.setSenderId(requesterVault.getId());
		message.setFrom(requesterVault.getPublicKey());
		message.setTo(remoteUserPublicKey);
		message.setOperation(SendMultivaluedPollRequestHandler.class.getName());
		
		message.putValue("poll", Vaultage.Gson.toJson(poll));
		
		this.requesterVault.getSendMultivaluedPollResponseBaseHandler().setCallerThread(Thread.currentThread());
		
		this.requesterVault.getVaultage().sendMessage(message.getTo(), requesterVault.getPublicKey(),
		requesterVault.getPrivateKey(), message);
		
		synchronized (Thread.currentThread()) {
			Thread.currentThread().wait();
		}
		
		return (List<Integer>) requesterVault.getSendMultivaluedPollResponseBaseHandler().getResult();
		
	}
	
	
	
	
}