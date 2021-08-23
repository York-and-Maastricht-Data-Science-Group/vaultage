package org.eclipse.epsilon.emc.vaultage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.FeatureCallExpression;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
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
import org.eclipse.epsilon.eol.execute.context.SingleFrame;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.eol.execute.introspection.java.ObjectMethod;
import org.eclipse.epsilon.eol.execute.operations.AbstractOperation;
import org.eclipse.epsilon.eol.execute.operations.contributors.IOperationContributorProvider;
import org.eclipse.epsilon.eol.execute.operations.contributors.OperationContributor;
import org.eclipse.epsilon.eol.execute.operations.simple.SimpleOperation;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.types.EolMap;
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
//		System.out.println("ALFA Operation: " + operationName);
		final ExecutorFactory executorFactory = context.getExecutorFactory();

		if (operationName.equals("includingAll")) {
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

		if (operationName.equals("includingAll")) {
			System.console();
		}

		// query remote vault
		if (targetObject instanceof RemoteVault) {
			VaultageOperationContributor op = (VaultageOperationContributor) context.getOperationContributorRegistry()
					.stream().filter(o -> o.getClass().equals(VaultageOperationContributor.class)).findFirst()
					.orElse(null);

			if (op != null) {
				targetObject = queryRemoteVault(context, targetObject, queryTargetExpression, op);
			}
			if (parameterExpressions.size() == 0) {
				return targetObject;
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

			ArrayList<Object> parameterValues = new ArrayList<>(parameterExpressions.size());

			for (Expression parameter : parameterExpressions) {
				parameterValues.add(executorFactory.execute(parameter, context));
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

//	@Override
//	public Object execute(IEolContext context) throws EolRuntimeException {
//		Object target = EolNoType.NoInstance;
//
//		String temp = this.getName();
//		System.out.println("Operation: " + temp);
//
//		Expression expression = null;
//		if (this.targetExpression instanceof FirstOrderOperationCallExpression) {
//			expression = targetExpression;
//		} else if (this.targetExpression instanceof FeatureCallExpression) {
//			expression = ((FeatureCallExpression) targetExpression).getTargetExpression();
//		}
//
//		if (expression != null) {
//			target = context.getExecutorFactory().execute(expression, context);
//		}
//
//		/***
//		 * If the target name expression cannot be found or it is not a NameExpression
//		 * then we use the super method.
//		 */
//		if (temp.equals("includingAll")) {
//			System.console();
//		}
//
//		if (target instanceof RemoteVault) {
//
//			VaultageOperationContributor op = (VaultageOperationContributor) context.getOperationContributorRegistry()
//					.stream().filter(o -> o.getClass().equals(VaultageOperationContributor.class)).findFirst()
//					.orElse(null);
//
//			if (op != null) {
//				Object result = queryRemoteVault(context, target, expression, op);
//				return result;
//			}
//		}
//
//		return super.execute(context);
//	}

	private Object queryRemoteVault(IEolContext context, Object target, Expression expression,
			VaultageOperationContributor op) throws VaultageEolRuntimeException, EolRuntimeException {
		/**
		 * Module has to be unparsed in its entirety to initialise all required objects.
		 * Otherwise, when parsing only certain expressions, it would fail, since the
		 * all preceding/required objects haven't been initialised.
		 */
		EolModule module = (EolModule) context.getModule();
		VaultageEolUnparser vaultageUnparser = new VaultageEolUnparser();
		vaultageUnparser.unparse(module);

		ModuleElement moduleElement = this;
		/**
		 * Get all variables
		 */
		EolMap<String, Object> variables = new EolMap<>();
		List<SingleFrame> frames = context.getFrameStack().getFrames(true);
		for (SingleFrame frame : frames) {
			for (Entry<String, Variable> entry : frame.getAll().entrySet()) {
				String name = entry.getKey();
				Variable var = entry.getValue();
				if (var.getValue() instanceof Boolean || var.getValue() instanceof Short
						|| var.getValue() instanceof Character || var.getValue() instanceof String
						|| var.getValue() instanceof Integer || var.getValue() instanceof Long
						|| var.getValue() instanceof Byte || var.getValue() instanceof Double
						|| var.getValue() instanceof Float) {
					variables.put(name, var.getValue());
				}
			}
		}

		/***
		 * Identify prefix statement (statement that returns remote vault) that will be
		 * replaced by local vault.
		 */
		String prefixStatement = vaultageUnparser.unparse(expression).trim();
//				System.out.println("prefix statement: " + prefixStatement);

		/***
		 * Construct a query string (EOL script) and treat all remote vault name
		 * expressions as a local vault.
		 */
		String localVaultClass = ((RemoteVault) target).getLocalVault().getClass().getSimpleName();
		String statement = vaultageUnparser.unparse(moduleElement).trim();
//				System.out.println("statement: " + statement);

		/***
		 * prevent sending local user-defined operations to a remote vault
		 */
		if (vaultageUnparser.getUserDefinedOperations().size() > 0) {
			String opName = vaultageUnparser.getUserDefinedOperations().get(0).getName();
			throw new VaultageEolRuntimeException("Sending user-defined operation '" //
					+ opName + "()' to a remote vault is not allowed.");
		}
		String vaultVariable = "localVault";
		statement = "var " + vaultVariable + " = " + localVaultClass + ".all.first;\n" //
				+ " return " + statement.replace(prefixStatement, vaultVariable) + ";";

		try {
			/***
			 * Call the Vault's Query operation
			 */
			String query = statement;
			String queryMethod = "query";

			target.getClass().getMethod(queryMethod, new Class<?>[] { String.class, Map.class });
			Object result = op.execute(target, queryMethod, new Object[] { query, variables });
			return result;
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
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
