package org.eclipse.epsilon.emc.vaultage;

import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.vaultage.core.RemoteVault;

public class VaultagePropertyCallExpression extends PropertyCallExpression {
	@Override
	public Object execute(Object source, NameExpression propertyNameExpression, IEolContext context)
			throws EolRuntimeException {

		if (source instanceof RemoteVault) {
			VaultageOperationContributor op = (VaultageOperationContributor) context.getOperationContributorRegistry()
					.stream().filter(o -> o.getClass().equals(VaultageOperationContributor.class)).findFirst()
					.orElse(null);

			if (op != null) {
				String name = new String(propertyNameExpression.getName());
				if (!name.startsWith("get")) {
					name = "get" + name.replaceFirst(name.substring(0, 1), name.substring(0, 1).toUpperCase());
				}
				try {
					source.getClass().getMethod(name, new Class<?>[] {});
					return op.execute(source, name, new Object[] {});
				} catch (NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
			}
		}
		return super.execute(source, propertyNameExpression, context);
	}
}