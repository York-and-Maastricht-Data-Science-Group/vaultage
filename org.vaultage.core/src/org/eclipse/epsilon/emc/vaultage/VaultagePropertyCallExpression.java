package org.eclipse.epsilon.emc.vaultage;

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
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.context.SingleFrame;
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

		if (source instanceof RemoteVault) {

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

				/***
				 * if the the target expression is the current expression (e.g., in
				 * remoteVault.posts.select(), the posts' parent expression is select(), but the
				 * select() operation's target expression is the posts since the results are
				 * returned back to the posts. operation's target is posts). In this case, we
				 * take select() as the expression to be unparsed as a whole; this will also
				 * unparsed the remoteVault and posts. Otherwise, the current expression will be
				 * unparsed (e.g., remoteVault.posts). The post will be unparsed as a whole;
				 * remoteVault is also included in the unparsed process.
				 */
				ModuleElement moduleElement = null;
				if (this.getParent() instanceof FirstOrderOperationCallExpression) {
//					parentTargetExpression = ((FirstOrderOperationCallExpression) this.getParent())
//							.getTargetExpression();
					moduleElement = this.getParent();
//				} 
//				else if (this.getParent() instanceof OperationCallExpression) {
////					parentTargetExpression = ((OperationCallExpression) this.getParent()).getTargetExpression();
//					moduleElement = this;
				} else {
					moduleElement = this;
				}

//				if (parentTargetExpression.equals(this)) {
//					moduleElement = this.getParent();
//				} else {
//					moduleElement = this;
//				}

				/**
				 * Get all variables
				 */
				EolMap<String, Object> variables = new EolMap<>();
				for (SingleFrame frame : context.getFrameStack().getFrames(true)) {
					for (Entry<String, Variable> entry : frame.getAll().entrySet()) {
						String name = entry.getKey();
						Variable var = entry.getValue();
						if (var.getValue() instanceof Boolean || var.getValue() instanceof String
								|| var.getValue() instanceof Integer || var.getValue() instanceof Double
								|| var.getValue() instanceof Float) {
							variables.put(name, var.getValue());
						}
					}
				}

				/***
				 * Construct a query string (EOL script) and treat all remote vault name
				 * expressions as a local vault.
				 */
				String target = "rv";
//				if (this.getTargetExpression() instanceof NameExpression) {
//					target = ((NameExpression) this.getTargetExpression()).getName();
//				} else {
//					target = "rv";
//				}
				String localVaultClass = ((RemoteVault) source).getLocalVault().getClass().getSimpleName();
				String statement = vaultageUnparser.unparse(moduleElement);
				statement = target + "." + statement.substring(statement.indexOf(propertyNameExpression.getName()));
				statement = "var " + target + " = " + localVaultClass + ".all.first;\n return " + statement + ";";

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

			// "rv.posts.parallelCollect(postId | rv.getPost(postId))";

//			VaultageOperationContributor op = (VaultageOperationContributor) context.getOperationContributorRegistry()
//					.stream().filter(o -> o.getClass().equals(VaultageOperationContributor.class)).findFirst()
//					.orElse(null);
//
//			if (op != null) {
//				String name = new String(propertyNameExpression.getName());
//				if (!name.startsWith("get")) {
//					name = "get" + name.replaceFirst(name.substring(0, 1), name.substring(0, 1).toUpperCase());
//				}
//				try {
//					source.getClass().getMethod(name, new Class<?>[] {});
//					return op.execute(source, name, new Object[] {});
//				} catch (NoSuchMethodException | SecurityException e) {
//					e.printStackTrace();
//				}
//			}
		}
		return super.execute(source, propertyNameExpression, context);
	}
}