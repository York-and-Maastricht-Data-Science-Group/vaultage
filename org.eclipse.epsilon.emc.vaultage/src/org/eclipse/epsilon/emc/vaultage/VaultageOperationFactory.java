package org.eclipse.epsilon.emc.vaultage;

import org.eclipse.epsilon.eol.execute.operations.EolOperationFactory;

public class VaultageOperationFactory extends EolOperationFactory {

	public VaultageOperationFactory() {
		super();
		operationCache.put("asyncCollect", new AsyncCollectOperation());
	}
}
