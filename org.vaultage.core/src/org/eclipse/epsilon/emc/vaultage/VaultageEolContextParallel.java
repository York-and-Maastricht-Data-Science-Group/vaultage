package org.eclipse.epsilon.emc.vaultage;

import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.context.concurrent.EolContextParallel;
import org.eclipse.epsilon.eol.execute.context.concurrent.IEolContextParallel;

public class VaultageEolContextParallel extends EolContextParallel implements IEolContextParallel {
	
	public VaultageEolContextParallel() {
		super();
	}
	
	public VaultageEolContextParallel(int parallelism) {
		super(parallelism);
	}
	
	protected VaultageEolContextParallel(IEolContext other) {
		super(other);
	}
	
	@Override
	protected IEolContext createShadowThreadLocalContext() {
		return new VaultageEolContextParallel(this);
	}

}
