package org.eclipse.epsilon.emc.vaultage;

import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;

public class VaultageOperationCallExpression extends OperationCallExpression {

	public VaultageOperationCallExpression() {
		super();
	}
	
	public VaultageOperationCallExpression(boolean contextless) {
		super(contextless);
	}
	
	public VaultageOperationCallExpression(Expression targetExpression, NameExpression nameExpression, Expression... parameterExpressions) {
		super(targetExpression, nameExpression, parameterExpressions);
	}
	@Override
	public Object execute(IEolContext context) throws EolRuntimeException {
		return super.execute(context);
	}
}
