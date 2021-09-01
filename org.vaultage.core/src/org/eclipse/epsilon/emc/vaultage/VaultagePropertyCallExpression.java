package org.eclipse.epsilon.emc.vaultage;

import org.eclipse.epsilon.eol.dom.AssignmentStatement;
import org.eclipse.epsilon.eol.dom.ExpressionStatement;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.dom.ReturnStatement;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
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
				|| this.getParent() instanceof ReturnStatement //
				|| this.getParent() instanceof OperationCallExpression //
		) //
				&& source instanceof RemoteVault) {

			VaultageEolRemoteOperationFetcher messageSender = new VaultageEolRemoteOperationFetcher();
			return messageSender.queryRemoteVault(this, context, source, this.getTargetExpression());

		}
		return super.execute(source, propertyNameExpression, context);
	}
}