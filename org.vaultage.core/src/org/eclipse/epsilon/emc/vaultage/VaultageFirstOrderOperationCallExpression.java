package org.eclipse.epsilon.emc.vaultage;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.FrameStack;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.context.SingleFrame;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.eol.types.EolMap;
import org.eclipse.epsilon.eol.types.EolNoType;
import org.vaultage.core.RemoteVault;

public class VaultageFirstOrderOperationCallExpression extends FirstOrderOperationCallExpression {

	@Override
	public Object execute(IEolContext context) throws EolRuntimeException {
		Object target = EolNoType.Instance;

		/***
		 * Iteratively getting the target name expression.
		 */
		Expression targetNameExpression = this.targetExpression;
		while (!(targetNameExpression.getClass().equals(NameExpression.class))) {
			if (targetNameExpression instanceof PropertyCallExpression) {
				targetNameExpression = ((PropertyCallExpression) targetNameExpression).getTargetExpression();
			} else if (targetNameExpression instanceof OperationCallExpression) {
				targetNameExpression = ((OperationCallExpression) targetNameExpression).getTargetExpression();
			} else if (targetNameExpression instanceof FirstOrderOperationCallExpression) {
				targetNameExpression = ((FirstOrderOperationCallExpression) targetNameExpression).getTargetExpression();
			} else {
				break;
			}
		}

		/***
		 * If the target name expression cannot be found or it is not a NameExpression
		 * then we use the super method.
		 */
		if (targetNameExpression != null && targetNameExpression.getClass().equals(NameExpression.class)) {
			FrameStack scope = context.getFrameStack();
			Variable variable = scope.get(((NameExpression) targetNameExpression).getName());

			if (variable != null && variable.getValue() instanceof RemoteVault) {
				target = variable.getValue();

				Class<?>[] parameterClasses = null;
				String targetOperationName = null;
				if (this.getTargetExpression() instanceof OperationCallExpression) {
					OperationCallExpression targetOperationCallExpression = (OperationCallExpression) this
							.getTargetExpression();
					targetOperationName = targetOperationCallExpression.getName();

					ArrayList<Object> parameterValues = new ArrayList<>(
							targetOperationCallExpression.getParameterExpressions().size());

					for (Expression parameter : targetOperationCallExpression.getParameterExpressions()) {
						parameterValues.add(context.getExecutorFactory().execute(parameter, context));
					}

					Object[] temp = parameterValues.toArray();
					parameterClasses = new Class<?>[temp.length];
					for (int i = 0; i < temp.length; i++) {
						parameterClasses[i] = temp[i].getClass();
					}
				}

				Method method = null;
				if (targetOperationName != null) {
					try {
						method = target.getClass().getMethod(targetOperationName, parameterClasses);
					} catch (NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
					}
				}

				VaultageOperationContributor op = (VaultageOperationContributor) context
						.getOperationContributorRegistry().stream()
						.filter(o -> o.getClass().equals(VaultageOperationContributor.class)).findFirst().orElse(null);

				if (op != null && method == null) {

					/**
					 * Module has to be unparsed in its entirety to initialise all required objects.
					 * Otherwise, when parsing only certain expressions, it would fail, since the
					 * all preceding/required objects haven't been initialised.
					 */
					EolModule module = (EolModule) context.getModule();
					VaultageEolUnparser vaultageUnparser = new VaultageEolUnparser();
					vaultageUnparser.unparse(module);

					ModuleElement moduleElement = this;
					
//					/**
//					 * Get all variables
//					 */
//					EolMap<String, Object> variables = new EolMap<>();
//					List<SingleFrame> frames = context.getFrameStack().getFrames(true);
//					for (SingleFrame frame : frames) {
//						for (Entry<String, Variable> entry : frame.getAll().entrySet()) {
//							String name = entry.getKey();
//							Variable var = entry.getValue();
//							if (var.getValue() instanceof Boolean || var.getValue() instanceof Short
//									|| var.getValue() instanceof Character || var.getValue() instanceof String
//									|| var.getValue() instanceof Integer || var.getValue() instanceof Long
//									|| var.getValue() instanceof Byte || var.getValue() instanceof Double
//									|| var.getValue() instanceof Float) {
//								variables.put(name, var.getValue());
//							}
//						}
//					}
					
					/***
					 * Construct a query string (EOL script) and treat all remote vault name
					 * expressions as a local vault.
					 */
					String localVaultClass = ((RemoteVault) target).getLocalVault().getClass().getSimpleName();
					String statement = vaultageUnparser.unparse(moduleElement).trim();
					
					EolMap<String, Object> variables = vaultageUnparser.getInUseVariables();
					
//					System.out.println("Send statement: " + statement);

					/***
					 * prevent sending local user-defined operations to a remote vault
					 */
					if (vaultageUnparser.getUserDefinedOperations().size() > 0) {
						String operationName = vaultageUnparser.getUserDefinedOperations().get(0).getName();
						throw new VaultageEolRuntimeException("Sending user-defined operation '" //
								+ operationName + "()' to a remote vault is not allowed.");
					}
					String vaultVariable = ((NameExpression) targetNameExpression).getName();
					statement = "var " + vaultVariable + " = " + localVaultClass + ".all.first;\n" //
							+ " return " + statement + ";";

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
				}

			}
		}

		return super.execute(context);
	}
}
