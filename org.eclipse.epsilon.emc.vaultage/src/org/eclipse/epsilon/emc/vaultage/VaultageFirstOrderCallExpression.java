package org.eclipse.epsilon.emc.vaultage;

import java.util.Collection;

import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.exceptions.EolNullPointerException;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.operations.AbstractOperation;
import org.eclipse.epsilon.eol.execute.operations.declarative.CollectBasedOperation;
import org.eclipse.epsilon.eol.execute.operations.declarative.CollectOperation;
import org.eclipse.epsilon.eol.execute.operations.declarative.SelectBasedOperation;
import org.eclipse.epsilon.eol.execute.operations.declarative.SelectOperation;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.types.EolModelElementType;
import org.eclipse.epsilon.eol.types.EolNoType;
import org.eclipse.epsilon.eol.types.EolType;
import org.vaultage.core.Vault;

public class VaultageFirstOrderCallExpression extends FirstOrderOperationCallExpression {

	@Override
	public Object execute(IEolContext context) throws EolRuntimeException {
		Object target = EolNoType.Instance;

		if (targetExpression != null) {
			target = context.getExecutorFactory().execute(targetExpression, context);
		} else if (!parameters.isEmpty()) {
			EolType iterator = parameters.get(0).getType(context);
			if (iterator instanceof EolModelElementType) {
				target = ((EolModelElementType) iterator).getAllOfKind();
			}
		}

		String operationName = nameExpression.getName();

		if (target == null) {
			if (isNullSafe()) {
				return null;
			} else {
				throw new EolNullPointerException(operationName, targetExpression);
			}
		}

		if (target instanceof Collection<?> && operationName.equals("asyncCollect")) {
			AbstractOperation operation = context.getOperationFactory().getOperationFor(operationName);
			return operation.execute(target, nameExpression, parameters, expressions, context);
		} else {
			return super.execute(context);
		}
	}

}
