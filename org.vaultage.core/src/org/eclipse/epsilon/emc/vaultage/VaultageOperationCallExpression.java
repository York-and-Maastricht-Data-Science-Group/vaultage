package org.eclipse.epsilon.emc.vaultage;

import java.util.ArrayList;

import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.OperationList;
import org.eclipse.epsilon.eol.dom.Parameter;
import org.eclipse.epsilon.eol.exceptions.EolIllegalOperationException;
import org.eclipse.epsilon.eol.exceptions.EolIllegalPropertyException;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.EolUndefinedVariableException;
import org.eclipse.epsilon.eol.execute.ExecutorFactory;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.introspection.java.ObjectMethod;
import org.eclipse.epsilon.eol.execute.operations.AbstractOperation;
import org.eclipse.epsilon.eol.execute.operations.contributors.IOperationContributorProvider;
import org.eclipse.epsilon.eol.execute.operations.contributors.OperationContributor;
import org.eclipse.epsilon.eol.execute.operations.simple.SimpleOperation;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.types.EolNoType;
import org.eclipse.epsilon.eol.types.EolSequence;
import org.eclipse.epsilon.eol.types.EolUndefined;
import org.vaultage.core.RemoteVault;

public class VaultageOperationCallExpression extends OperationCallExpression {

	public VaultageOperationCallExpression() {
		this(false);
	}

	public VaultageOperationCallExpression(boolean contextless) {
		super(contextless);
	}

	@Override
	public Object execute(IEolContext context) throws EolRuntimeException {
		Object targetObject;
		String operationName = nameExpression.getName();
		System.out.println("ALFA Operation: " + operationName);
		final ExecutorFactory executorFactory = context.getExecutorFactory();

		if (operationName.equals("getPost")) {
			System.console();
		}

		Expression queryTargetExpression = targetExpression;

		if (!contextless) {
			try {
				targetObject = executorFactory.execute(this.targetExpression, context);
			} catch (EolUndefinedVariableException | EolIllegalPropertyException npe) {
				switch (operationName) {
				default:
					throw npe;
				case "isDefined":
				case "isUndefined":
				case "ifDefined":
				case "ifUndefined": {
					targetObject = EolUndefined.INSTANCE;
					break;
				}
				}
			}
		} else {
			targetObject = EolNoType.NoInstance;
		}

		if (targetObject == null && isNullSafe()) {
			return null;
		}

//		targetObject = context.getExecutorFactory().execute(queryTargetExpression, context);

		if (operationName.equals("getPost")) {
			System.console();
		}

		ArrayList<Object> parameterValues = new ArrayList<>(parameterExpressions.size());

		for (Expression parameter : parameterExpressions) {
			Object value = executorFactory.execute(parameter, context);
			parameterValues.add(value);
//			context.getFrameStack().put(((NameExpression) parameter).getName(), value);
		}

		// query remote vault
		if (targetObject instanceof RemoteVault) {
			if (!operationName.equals("query")) {
				VaultageEolRemoteOperationFetcher messageSender = new VaultageEolRemoteOperationFetcher();
				if (queryTargetExpression instanceof NameExpression) {
					targetObject = messageSender.executeRemoteVaultOperation(targetObject, context, operationName,
							parameterValues);
					return targetObject;
				} else {
					targetObject = messageSender.queryRemoteVault(this, context, targetObject, queryTargetExpression);
				}
				if (parameterExpressions.size() == 0) {
					return targetObject;
				}
			}
		}

		IModel owningModel = context.getModelRepository().getOwningModel(targetObject);

		// Non-overridable operations
		AbstractOperation operation = getAbstractOperation(targetObject, operationName, owningModel, context);
		if (operation != null && !operation.isOverridable()) {
			return operation.execute(targetObject, nameExpression, new ArrayList<Parameter>(0), parameterExpressions,
					context);
		}

		// Operation contributor for model elements
		OperationContributor operationContributor = null;

		// Method contributors that use the unevaluated AST
		ObjectMethod objectMethod = null;

		try {
			if (targetObject instanceof IOperationContributorProvider) {
				operationContributor = ((IOperationContributorProvider) targetObject).getOperationContributor();
			} else if (owningModel != null && owningModel instanceof IOperationContributorProvider) {
				operationContributor = ((IOperationContributorProvider) owningModel).getOperationContributor();
			}

			if (operationContributor != null) {
				objectMethod = operationContributor.findContributedMethodForUnevaluatedParameters(targetObject,
						operationName, parameterExpressions, context);
			}
			if (objectMethod == null) {
				objectMethod = context.getOperationContributorRegistry().findContributedMethodForUnevaluatedParameters(
						targetObject, operationName, parameterExpressions, context);
			}

			if (objectMethod != null) {
				return wrap(objectMethod.execute(nameExpression, context, nameExpression));
			}

			Object module = context.getModule();
			// Execute user-defined operation (if isArrow() == false)
			if (module instanceof IEolModule && !isArrow()) {
				OperationList operations = ((IEolModule) module).getOperations();
				Operation helper = operations.getOperation(targetObject, nameExpression, parameterValues, context);
				if (helper != null) {
					return helper.execute(targetObject, parameterValues, context);
				}
			}

			Object[] parameterValuesArray = parameterValues.toArray();

			// Method contributors that use the evaluated parameters
			if (operationContributor != null) {
				// Try contributors that override the context's operation contributor registry
				objectMethod = operationContributor.findContributedMethodForEvaluatedParameters(targetObject,
						operationName, parameterValuesArray, context, true);
			}

			if (objectMethod == null) {
				objectMethod = context.getOperationContributorRegistry().findContributedMethodForEvaluatedParameters(
						targetObject, operationName, parameterValuesArray, context);
			}

			if (operationContributor != null && objectMethod == null) {
				// Try contributors that do not override the context's operation contributor
				// registry
				objectMethod = operationContributor.findContributedMethodForEvaluatedParameters(targetObject,
						operationName, parameterValuesArray, context, false);
			}
			if (objectMethod != null) {
				return wrap(objectMethod.execute(nameExpression, context, parameterValuesArray));
			}

			// Execute user-defined operation (if isArrow() == true)
			if (operation instanceof SimpleOperation) {
				return ((SimpleOperation) operation).execute(targetObject, parameterValues, context, nameExpression);
			}

			// Most likely a FirstOrderOperation or DynamicOperation
			if (operation != null && targetObject != null && !parameterExpressions.isEmpty()) {
				return operation.execute(targetObject, nameExpression, new ArrayList<>(0), parameterExpressions,
						context);
			}

			// No operation found
			throw new EolIllegalOperationException(targetObject, operationName, nameExpression,
					context.getPrettyPrinterManager());
		} finally {
			// Clean up ThreadLocal
			if (operationContributor != null) {
				operationContributor.close();
			}
			if (objectMethod != null) {
				objectMethod.close();
			}
		}

	}

	static Object wrap(Object o) {
		if (o instanceof Object[]) {
			Object[] arr = (Object[]) o;
			EolSequence<Object> seq = new EolSequence<>();
			seq.ensureCapacity(arr.length);
			for (Object element : arr) {
				seq.add(element);
			}
			return seq;
		} else
			return o;
	}
}
