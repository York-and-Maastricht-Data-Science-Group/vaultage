package org.eclipse.epsilon.emc.vaultage;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.FeatureCallExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.context.SingleFrame;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.eol.types.EolMap;
import org.eclipse.epsilon.eol.types.EolNoType;
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
		Object target = EolNoType.NoInstance;

		String namex = this.getNameExpression().getName();
		
		Expression expression = null;
		if (targetExpression instanceof FeatureCallExpression) {
			expression = ((FeatureCallExpression) targetExpression).getTargetExpression();
			if (expression != null) {
				target = context.getExecutorFactory().execute(expression, context);
			}
		}

		/***
		 * If the target name expression cannot be found or it is not a NameExpression
		 * then we use the super method.
		 */

		if (target != null && target instanceof RemoteVault) {

			VaultageOperationContributor op = (VaultageOperationContributor) context.getOperationContributorRegistry()
					.stream().filter(o -> o.getClass().equals(VaultageOperationContributor.class)).findFirst()
					.orElse(null);

			if (op != null) {

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
			}

		}

		return super.execute(context);
	}
}
