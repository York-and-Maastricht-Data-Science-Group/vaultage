package org.eclipse.epsilon.emc.vaultage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.introspection.java.ObjectMethod;
import org.eclipse.epsilon.eol.execute.operations.contributors.OperationContributor;
import org.eclipse.epsilon.eol.types.EolNoType;
import org.eclipse.epsilon.eol.util.ReflectionUtil;
import org.vaultage.core.OperationResponseHandler;
import org.vaultage.core.RemoteVault;
import org.vaultage.core.Vault;
import org.vaultage.demo.fairnet.Friend;

public class VaultageOperationContributor extends OperationContributor {

	private static final String RESPONSE_HANDLER = "ResponseHandler";
	public static final int DEFAULT_TIMEOUT = 60000; // milliseconds
	private static int timeout = DEFAULT_TIMEOUT;

	/***
	 * This operation contributor only contributes to EolNoType and RemoteVault. 
	 */
	@Override
	public boolean contributesTo(Object target) {
		if (target == EolNoType.NoInstance || target instanceof RemoteVault) {
			return true;
		} else {
			return false;
		}
	}

//	@Override
//	protected Object getReflectionTarget(Object target) {
//		if (target instanceof RemoteVault) {
//			return target;
//		}
//		return super.getReflectionTarget(target);
//	}

	/***
	 * Overrides the same method of the parent class (OperationContributor). If the
	 * target object is an instace of RemoteVault then the 'createObjectMethodFor'
	 * of this class called, otherwise the parent class' 'createObjectMethodFor' called.
	 */
	@Override
	public ObjectMethod findContributedMethodForEvaluatedParameters(Object target, String name, Object[] parameters,
			IEolContext context, boolean overrideContextOperationContributorRegistry) {
		if (target instanceof RemoteVault) {
			return this.createObjectMethodFor(target, name, parameters, context,
					overrideContextOperationContributorRegistry);
		} else {
			return super.findContributedMethodForEvaluatedParameters(target, name, parameters, context,
					overrideContextOperationContributorRegistry);
		}
	}

	/***
	 * Overrides the same method of the parent class (OperationContributor) so that
	 * whatever method called, only the 'execute' method in this
	 * VaultageContributorOperation class that will be executed. The initial
	 * method's target, name, and parameters will be passed as the parameters of the
	 * 'execute' method.
	 * 
	 * @param target
	 * @param name
	 * @param parameters
	 * @param context
	 * @param allowContravariantConversionForParameters
	 * @return
	 */
	ObjectMethod createObjectMethodFor(Object target, String name, Object[] parameters, IEolContext context,
			boolean allowContravariantConversionForParameters) {
		Method method = null;

		// Maintain a cache of method names if the reflection target is this
		// so that we don't iterate through all methods every time
		if (getReflectionTarget(target) == this && cachedMethodNames == null)
			synchronized (this) {
				if (cachedMethodNames == null) {
					cachedMethodNames = ReflectionUtil.getMethodNames(this, includeInheritedMethods());
				}
			}

		/*
		 * Here we replace the name of the called method with 'execute' which is the
		 * 'execute' method of this class. So that when any methods of RemoteVault to be
		 * called, we localised it that only this method is executed. In the parent
		 * class, the real/initial method's name is used.
		 */
		String executeName = "execute";
		Object[] executeParameters = { target, executeName, parameters };

		method = ReflectionUtil.getMethodFor(getReflectionTarget(this), executeName, executeParameters,
				includeInheritedMethods(), true);

		if (method != null) {
			/*
			 * Here we set the parameters of the 'execute' method of this class. We use the
			 * real/initial name of the method. You can see the each value in the
			 * 'methodParameters' corresponds to each parameter of the 'execute' method of
			 * this class.
			 */
			Object[] methodParameters = { target, name, parameters };

			Object reflectionTarget = this;
			/*
			 * VaultageObjectMethod which is an extended class of the ObjectMethod class.
			 * The class allows to pass the values of the parameters instead of just passing
			 * the reflection target and method.
			 */
			ObjectMethod objectMethod = new VaultageObjectMethod(reflectionTarget, method, methodParameters);

			/*
			 * If the reflection target is this contributor, then it will need to know about
			 * the actual operand for the method and the intended context.
			 */
			if (reflectionTarget == this) {
				setTarget(target);
				setContext(context);
			}
			return objectMethod;
		} else
			return null;
	}

	/***
	 * The method to execute any methods of RemoteVault. The method's target, name,
	 * and parameters' values are passed as this method's parameters. Reflection is
	 * used to determine the RemoteVault's method.
	 * 
	 * @param target
	 * @param name
	 * @param parameters
	 * @return
	 * @throws EolRuntimeException
	 */
	public Object execute(Object target, String name, Object[] parameters) throws EolRuntimeException {
//		System.out.println("Executing method " + name);
		Object result = null;
		if (target instanceof RemoteVault) {
			RemoteVault remoteVault = (RemoteVault) target;
			Method method = null;
			try {
				Class<?>[] parametersTypes = new Class<?>[parameters.length];
				for (int i = 0; i < parameters.length; i++) {
					parametersTypes[i] = parameters[i].getClass();
				}

				Vault localVault = remoteVault.getLocalVault();
				String temp = localVault.getClass().getPackageName() + "."
						+ name.replaceFirst(name.substring(0, 1), name.substring(0, 1).toUpperCase())
						+ RESPONSE_HANDLER;
				Class<?> responseHandlerClass = Class.forName(temp);
				OperationResponseHandler handler = localVault.getOperationResponseHandler(responseHandlerClass);
				synchronized (handler) {
					method = remoteVault.getClass().getMethod(name, parametersTypes);
					method.invoke(remoteVault, parameters);
					handler.wait();
				}
				result = handler.getResult();
				
			} catch (NoSuchMethodException | SecurityException | InterruptedException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException | ClassNotFoundException e) {
				new EolRuntimeException(e.getMessage());
			}
		}
		return result;
	}

	public String hello() {
		if (getTarget() instanceof Friend) {
			Friend friend = ((Friend) getTarget());
			return "Hello, " + friend.getName() + "!";
		}
		return "Hello!";
	}

	/**
	 * To get the timeout of the asynchronous query. The default value is
	 * VaultageOperationContributor.DEFAULT_TIMEOUT.
	 * 
	 * @return the timeout
	 */
	public static int getTimeout() {
		return timeout;
	}

	/**
	 * To set the timeout of the asynchronous query. The default value is
	 * VaultageOperationContributor.DEFAULT_TIMEOUT.
	 * 
	 * @param timeout the timeout to set
	 */
	public static void setTimeout(int timeout) {
		VaultageOperationContributor.timeout = timeout;
	}

}
