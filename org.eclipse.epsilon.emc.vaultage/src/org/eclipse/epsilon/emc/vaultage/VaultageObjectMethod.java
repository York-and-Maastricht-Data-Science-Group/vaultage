package org.eclipse.epsilon.emc.vaultage;

import java.lang.reflect.Method;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.introspection.java.ObjectMethod;
import org.eclipse.epsilon.eol.util.ReflectionUtil;

/***
 * Extends the ObjectMethod class so that we can also set the parameters of the
 * ObjectMethod instead of only able receiving it from the
 * OperationCallExpression. This class is made to accommodate the 'execute'
 * method of the VaultageOperationContributor class.
 * 
 * @author Alfa Yohannis
 *
 */
public class VaultageObjectMethod extends ObjectMethod {

	private Object[] parameters = null;

	/***
	 * We could also pass custom parameters parameter instead of just receiving it,
	 * which its values are determined in the OperationCallExpression class.
	 * 
	 * @param object
	 * @param method
	 * @param parameters
	 */
	public VaultageObjectMethod(Object object, Method method, Object[] parameters) {
		super(object, method);
		this.parameters = parameters;
	}

	/***
	 * Override the parent's method. Instead of using the parameters parameter (the
	 * values are determined in the OperationCallExpression class), the method use
	 * the parameters field that we can set as the parameters parameter.
	 */
	@Override
	public Object execute(Object[] parameters, ModuleElement ast) throws EolRuntimeException {
		return ReflectionUtil.executeMethod(object, method, ast, this.parameters);
	}

	/**
	 * @return the parameters
	 */
	public Object[] getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}
}
