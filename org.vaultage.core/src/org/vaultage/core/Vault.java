package org.vaultage.core;

public interface Vault {

	public String getId();

	public void setId(String id);

	public Vaultage getVaultage();

	public void setVaultage(Vaultage vaultage);

	public String getPrivateKey();

	public void setPrivateKey(String privateKey);

	public String getPublicKey();

	public void setPublicKey(String publicKey);
}
