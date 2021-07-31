package org.eclipse.epsilon.emc.vaultage;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.epsilon.common.module.IModule;
import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.parse.EolUnparser;
import org.eclipse.epsilon.eol.types.EolMap;
import org.vaultage.core.RemoteVault;
import org.vaultage.core.Vault;

/***
 * This class converts property call to calling an operation. For example:
 * x.posts.collect(...) to x.getPosts().collect(...).
 * 
 * @author Alfa Yohannis
 *
 */
public class VaultagePropertyCallExpression extends PropertyCallExpression {

	/***
	 * The method replace
	 */
	@Override
	public Object execute(Object source, NameExpression propertyNameExpression, IEolContext context)
			throws EolRuntimeException {

		if (source instanceof RemoteVault) {

			VaultageOperationContributor op = (VaultageOperationContributor) context.getOperationContributorRegistry()
					.stream().filter(o -> o.getClass().equals(VaultageOperationContributor.class)).findFirst()
					.orElse(null);

			if (op != null) {

				EolModule module = (EolModule) context.getModule();
				VaultageUnparser unparser = new VaultageUnparser();
				String code = unparser.unparse(module);
//				ModuleElement moduleElement =  propertyNameExpression;
				ModuleElement moduleElement = this;
//				ModuleElement moduleElement = this.getParent();

				Object target = ((NameExpression) this.getTargetExpression()).getName();
				String localVaultClass = ((RemoteVault) source).getLocalVault().getClass().getSimpleName();
				String statement = unparser.unparse(moduleElement);
				statement = "var " + target + " = " + localVaultClass + ".all.first;\n return " + statement + ";";

				try {
					String query = statement;
					String queryMethod = "query";
					EolMap<?, ?> map = new EolMap<>();
					source.getClass().getMethod(queryMethod, new Class<?>[] { String.class, Map.class });
					Object result = op.execute(source, queryMethod, new Object[] { query, map });
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