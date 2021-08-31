package org.eclipse.epsilon.emc.vaultage;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.FrameStack;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.context.Variable;
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

				if (method == null) {
					VaultageEolRemoteOperationFetcher messageSender = new VaultageEolRemoteOperationFetcher();
					 return messageSender.queryRemoteVault(this, context, target, targetNameExpression);
				}

			}
		}

		return super.execute(context);
	}
}
