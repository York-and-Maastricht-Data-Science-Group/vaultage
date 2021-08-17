package org.eclipse.epsilon.emc.vaultage;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.OperationList;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.ExecutorFactory;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.parse.EolUnparser;
import org.eclipse.epsilon.eol.types.EolNoType;

public class VaultageEolUnparser extends EolUnparser {

	// 'true' indicates that the unparsing only for sub-elements/expressions, not
	// for the entire module/script.
	private boolean isPartial = false;
	private final List<Operation> userDefinedOperations = new ArrayList<>();

	/***
	 * Move the buffer string to a temporary String then move it back to the buffer
	 * after creating the statement string of the expression.
	 * 
	 * @param moduleElement
	 * @return
	 */
	public String unparse(ModuleElement moduleElement) {
		// indicating that the unparsing only for sub-expressions, not for the entire
		// module/script.
		isPartial = true;
		userDefinedOperations.clear();
		
		String originalBuffer = new String(buffer.toString());

		buffer.setLength(0);
		if (moduleElement instanceof FirstOrderOperationCallExpression) {
			this.visit((FirstOrderOperationCallExpression) moduleElement);
		} else if (moduleElement instanceof OperationCallExpression) {
			this.visit((OperationCallExpression) moduleElement);
		} else if (moduleElement instanceof PropertyCallExpression) {
			this.visit((PropertyCallExpression) moduleElement);
		} else if (moduleElement instanceof NameExpression) {
			this.visit((NameExpression) moduleElement);
		}
		String statement = new String(buffer.toString());

		buffer.setLength(0);
		buffer.append(originalBuffer);

		isPartial = false;

		return statement;
	}

	@Override
	public void visit(OperationCallExpression operationCallExpression) {
		super.visit(operationCallExpression);
		if (isPartial) {
			EolModule module = (EolModule) operationCallExpression.getModule();
			OperationList operations = module.getDeclaredOperations();

			String operationName = operationCallExpression.getName();
			IEolContext context = module.getContext();
			Object targetObject = EolNoType.NoInstance;
			List<Expression> parameterExpressions = operationCallExpression.getParameterExpressions();
			ArrayList<Object> parameterValues = new ArrayList<>(parameterExpressions.size());
			ExecutorFactory executorFactory = context.getExecutorFactory();
			try {
				for (Expression parameter : parameterExpressions) {
					parameterValues.add(executorFactory.execute(parameter, context));
				}
				if (module instanceof IEolModule && !operationCallExpression.isArrow()) {
					Operation helper = operations.getOperation(targetObject, operationName, parameterValues, context);
					if (helper != null) {
						userDefinedOperations.add(helper);
					}
				}
			} catch (EolRuntimeException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the userDefinedOperations
	 */
	public List<Operation> getUserDefinedOperations() {
		return userDefinedOperations;
	}
}
