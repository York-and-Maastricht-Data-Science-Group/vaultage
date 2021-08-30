package org.eclipse.epsilon.emc.vaultage;

import java.util.Map;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.dom.AssignmentStatement;
import org.eclipse.epsilon.eol.dom.ExpressionStatement;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.eol.types.EolMap;
import org.vaultage.core.RemoteVault;

/***
 * This class is responsible to automatically infer vaults if they are local
 * vaults and remote vaults. If a vault is a remote vault then requests will be
 * sent to the remote vaults to retrieve the data. If the vault is a local vault
 * then the data will be retrieved locally. For example:
 * 
 * localVault.posts -> this will retrieve posts from the local vault
 * remoteVault.posts -> this will send request to the remote vault by sending
 * sub expressions to the remote vault to be executed locally in the remote
 * vault.
 * 
 * @author Alfa Yohannis
 *
 */
public class VaultagePropertyCallExpression extends PropertyCallExpression {

	@Override
	public Object execute(Object source, NameExpression propertyNameExpression, IEolContext context)
			throws EolRuntimeException {

		if ((this.getParent() instanceof FirstOrderOperationCallExpression //
				|| this.getParent() instanceof AssignmentStatement //
				|| this.getParent() instanceof ExpressionStatement //
				|| this.getParent() instanceof OperationCallExpression //
				) //
				&& source instanceof RemoteVault) {

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

//				/**
//				 * Get all variables
//				 */
//				EolMap<String, Object> variables = new EolMap<>();
//				List<SingleFrame> frames = context.getFrameStack().getFrames(true);
//				for (SingleFrame frame : frames) {
//					for (Entry<String, Variable> entry : frame.getAll().entrySet()) {
//						String name = entry.getKey();
//						Variable var = entry.getValue();
//						if (var.getValue() instanceof Boolean || var.getValue() instanceof Short
//								|| var.getValue() instanceof Character || var.getValue() instanceof String
//								|| var.getValue() instanceof Integer || var.getValue() instanceof Long
//								|| var.getValue() instanceof Byte || var.getValue() instanceof Double
//								|| var.getValue() instanceof Float) {
//							variables.put(name, var.getValue());
//						}
//					}
//				}

				/***
				 * Construct a query string (EOL script) and treat all remote vault name
				 * expressions as a local vault.
				 */
				String target = "rv";
				String localVaultClass = ((RemoteVault) source).getLocalVault().getClass().getSimpleName();
				String statement = vaultageUnparser.unparse(moduleElement).trim();
				EolMap<String, Object> variables = vaultageUnparser.getInUseVariables();
//				System.out.println(statement);

				/***
				 * construct origin variable to identify the origin of propagated/chained
				 * messages
				 */
				Variable origin = context.getFrameStack().getGlobal(VaultageModel.ORIGIN_STRING);
				if (origin == null) {
					variables.put(VaultageModel.ORIGIN_STRING,
							((RemoteVault) source).getLocalVault().getPublicKey());
				}
				
				/***
				 * prevent sending local user-defined operations to a remote vault
				 */
				if (vaultageUnparser.getUserDefinedOperations().size() > 0) {
					String operationName = vaultageUnparser.getUserDefinedOperations().get(0).getName();
					throw new VaultageEolRuntimeException("Sending user-defined operation '" //
							+ operationName + "()' to a remote vault is not allowed.");
				}

				statement = target + "." + statement.substring(statement.indexOf(propertyNameExpression.getName()));
				statement = "var " + target + " = " + localVaultClass + ".all.first;\n" //
						+ "return " + statement + ";";

				try {
					/***
					 * Call the Vault's Query operation
					 */
					String query = statement;
					String queryMethod = "query";

					source.getClass().getMethod(queryMethod, new Class<?>[] { String.class, Map.class });
					Object result = op.execute(source, queryMethod, new Object[] { query, variables });
					return result;
				} catch (NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
			}

		}
		return super.execute(source, propertyNameExpression, context);
	}
}