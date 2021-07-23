package org.vaultage.core;

import java.util.UUID;

public abstract class Entity {
	
	protected String id = UUID.randomUUID().toString();

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return id;
	}
}
