package org.vaultage.demo.spreadcut.gen;

import java.util.List;

import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageMessage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.spreadcut.app.SpreadCutter;

import org.vaultage.demo.spreadcut.app.GetContactsRequestHandler;
import org.vaultage.demo.spreadcut.app.GetContactsResponseHandler;	
import org.vaultage.demo.spreadcut.app.ConfirmContactRequestHandler;
import org.vaultage.demo.spreadcut.app.ConfirmContactResponseHandler;	

public class RemoteRequester {

	protected VaultageServer vaultageServer;
	protected SpreadCutter requesterVault;
	
	public RemoteRequester(VaultageServer vaultageServer, SpreadCutter vault) throws Exception {
		this.vaultageServer = vaultageServer;
		this.requesterVault = vault;
	}
	
	public List<ProximityContact> getContacts(String requesteePublicKey, String timestamp) throws Exception {
	
		VaultageMessage message = new VaultageMessage();
		message.setSenderId(requesterVault.getId());
		message.setFrom(requesterVault.getPublicKey());
		message.setTo(requesteePublicKey);
		message.setOperation(GetContactsRequestHandler.class.getName());
		
		message.putValue("timestamp", Vaultage.Gson.toJson(timestamp));
		
		this.requesterVault.getGetContactsResponseBaseHandler().setCallerThread(Thread.currentThread());
		
		this.requesterVault.getVaultage().sendMessage(message.getTo(), requesterVault.getPublicKey(),
		requesterVault.getPrivateKey(), message);
		
		synchronized (Thread.currentThread()) {
			Thread.currentThread().wait();
		}
		
		return (List<ProximityContact>) requesterVault.getGetContactsResponseBaseHandler().getResult();
		
	}
	
	public boolean confirmContact(String requesteePublicKey, String timestamp) throws Exception {
	
		VaultageMessage message = new VaultageMessage();
		message.setSenderId(requesterVault.getId());
		message.setFrom(requesterVault.getPublicKey());
		message.setTo(requesteePublicKey);
		message.setOperation(ConfirmContactRequestHandler.class.getName());
		
		message.putValue("timestamp", Vaultage.Gson.toJson(timestamp));
		
		this.requesterVault.getConfirmContactResponseBaseHandler().setCallerThread(Thread.currentThread());
		
		this.requesterVault.getVaultage().sendMessage(message.getTo(), requesterVault.getPublicKey(),
		requesterVault.getPrivateKey(), message);
		
		synchronized (Thread.currentThread()) {
			Thread.currentThread().wait();
		}
		
		return (boolean) requesterVault.getConfirmContactResponseBaseHandler().getResult();
		
	}
	
	
	
	
}