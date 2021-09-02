package org.eclipse.epsilon.emc.vaultage;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.eol.types.EolMap;
import org.eclipse.epsilon.eol.types.EolNoType;

/***
 * A class that is responsible to fetch the operations, query or other
 * operations, of a remote vault.
 * 
 * @author Alfa Yohannis
 *
 */
public class VaultageEolRemoteOperationFetcher {

	/***
	 * Execute an operation of a remote vault.
	 * 
	 * @param target
	 * @param context
	 * @param methodName
	 * @param parameterValues
	 * @return
	 */
	public Object executeRemoteVaultOperation(Object target, IEolContext context, String methodName,
			ArrayList<Object> parameterValues) {
		Object result = EolNoType.NoInstance;

		VaultageOperationContributor op = (VaultageOperationContributor) context.getOperationContributorRegistry()
				.stream().filter(o -> o.getClass().equals(VaultageOperationContributor.class)).findFirst().orElse(null);
		if (op == null)
			return result;

		try {
			result = op.execute(target, methodName, parameterValues.toArray());
			if (result == null) {
				return EolNoType.NoInstance;
			}
			return result;
		} catch (EolRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return EolNoType.NoInstance;
	}

	/***
	 * Execute the query operation of a remote vault.
	 * 
	 * @param moduleElement
	 * @param context
	 * @param target
	 * @param queryTargetExpression
	 * @return
	 * @throws VaultageEolRuntimeException
	 * @throws EolRuntimeException
	 */
	public Object queryRemoteVault(ModuleElement moduleElement, IEolContext context, Object target,
			Expression queryTargetExpression) throws VaultageEolRuntimeException, EolRuntimeException {

		VaultageOperationContributor op = (VaultageOperationContributor) context.getOperationContributorRegistry()
				.stream().filter(o -> o.getClass().equals(VaultageOperationContributor.class)).findFirst().orElse(null);
		if (op == null)
			return EolNoType.NoInstance;

		/**
		 * Module has to be unparsed in its entirety to initialise all required objects.
		 * Otherwise, when parsing only certain expressions, it would fail, since the
		 * all preceding/required objects haven't been initialised.
		 */
		EolModule module = (EolModule) context.getModule();
		VaultageEolUnparser vaultageUnparser = new VaultageEolUnparser();
		vaultageUnparser.unparse(module);

		/***
		 * Identify prefix statement (statement that returns remote vault) that will be
		 * replaced by local vault.
		 * 
		 */
		String prefixStatement = null;
		if (queryTargetExpression != null) {
			prefixStatement = vaultageUnparser.unparse(queryTargetExpression).trim();
		}
//				System.out.println("prefix statement: " + prefixStatement);

		/***
		 * Construct a query string (EOL script) and treat all remote vault name
		 * expressions as a local vault.
		 */
		String statement = vaultageUnparser.unparse(moduleElement).trim();
		EolMap<String, Object> variables = vaultageUnparser.getInUseVariables();
//		System.out.println("statement: " + statement);

		/***
		 * construct origin variable to identify the origin of propagated/chained
		 * messages
		 */
		VaultageModel model = (VaultageModel) context.getModelRepository().getModelByName("M");
		Variable origin = context.getFrameStack().getGlobal(VaultageModel.ORIGIN_STRING);
		if (origin == null) {
			variables.put(VaultageModel.ORIGIN_STRING, model.getLocalVault().getPublicKey());
		}else {
			variables.put(VaultageModel.ORIGIN_STRING, origin.getValue());
		}

//		/***
//		 * prevent sending local user-defined operations to a remote vault
//		 */
//		if (vaultageUnparser.getUserDefinedOperations().size() > 0) {
//			String opName = vaultageUnparser.getUserDefinedOperations().get(0).getName();
//			throw new VaultageEolRuntimeException("Sending user-defined operation '" //
//					+ opName + "()' to a remote vault is not allowed.");
//		}
		String vaultVariable = "localVault";
		if (prefixStatement != null)
			statement = statement.replace(prefixStatement, vaultVariable);

		statement = "var " + vaultVariable + " = M.allContents().first();\n" //
				+ " return " + statement + ";\n";

		/***
		 * add declared operations as well
		 */
		for (Operation operation : module.getDeclaredOperations()) {
			statement = statement + vaultageUnparser.unparse(operation) + "\n";
		}

		try {
			/***
			 * Call the Vault's Query operation
			 */
			String query = statement;
			String queryMethod = "query";

			target.getClass().getMethod(queryMethod, new Class<?>[] { String.class, Map.class });
			Object result = op.execute(target, queryMethod, new Object[] { query, variables });
			if (result == null) {
				return EolNoType.NoInstance;
			}
			return result;
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return EolNoType.NoInstance;
	}
}
