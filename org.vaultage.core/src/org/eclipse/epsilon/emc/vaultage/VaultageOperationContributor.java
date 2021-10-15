package org.eclipse.epsilon.emc.vaultage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.introspection.java.ObjectMethod;
import org.eclipse.epsilon.eol.execute.operations.contributors.OperationContributor;
import org.eclipse.epsilon.eol.types.EolMap;
import org.eclipse.epsilon.eol.types.EolNoType;
import org.eclipse.epsilon.eol.types.EolSequence;
import org.eclipse.epsilon.eol.util.ReflectionUtil;
import org.vaultage.core.OperationResponseHandler;
import org.vaultage.core.RemoteVault;
import org.vaultage.core.Vault;

public class VaultageOperationContributor extends OperationContributor {

	private static final String RESPONSE_HANDLER = "ResponseHandler";
	public static final int DEFAULT_TIMEOUT = 600000; // milliseconds
	private static int timeout = DEFAULT_TIMEOUT;

	/***
	 * This operation contributor only contributes to EolNoType and RemoteVault.
	 */
	@Override
	public boolean contributesTo(Object target) {
		if (target == EolNoType.NoInstance || target instanceof RemoteVault || target instanceof Vault) {
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
	 * of this class called, otherwise the parent class' 'createObjectMethodFor'
	 * called.
	 */
	@Override
	public ObjectMethod findContributedMethodForEvaluatedParameters(Object target, String name, Object[] parameters,
			IEolContext context, boolean overrideContextOperationContributorRegistry) {
		if (target instanceof RemoteVault || target instanceof Vault) {
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

		Object result = null;
		if (target instanceof RemoteVault) {
//			try {
//				if (parameters.length > 0 && parameters[0].equals("alice-01")) {
//					Thread.sleep(1000);
//				}
//			} catch (InterruptedException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}

			RemoteVault remoteVault = (RemoteVault) target;
			Method method = null;
			try {
				Class<?>[] parametersTypes = new Class<?>[parameters.length];
				for (int i = 0; i < parameters.length; i++) {
					if (parameters[i] instanceof EolSequence) {
						parametersTypes[i] = List.class;
					} else if (parameters[i] instanceof EolMap) {
						parametersTypes[i] = Map.class;
					} else {
						parametersTypes[i] = parameters[i].getClass();
					}
				}

				String token = null;
				Vault localVault = remoteVault.getLocalVault();
				String baseName = name.replaceFirst(name.substring(0, 1), name.substring(0, 1).toUpperCase());
				String temp = localVault.getClass().getPackageName() + "." + baseName + RESPONSE_HANDLER;

				Class<?> responseHandlerClass = null;
				try {
					responseHandlerClass = Class.forName(temp);
				} catch (Exception e) {
					temp = Vault.class.getPackageName() + "." + baseName + RESPONSE_HANDLER;
					responseHandlerClass = Class.forName(temp);
				}
				OperationResponseHandler handler = localVault.getOperationResponseHandler(responseHandlerClass);
				Object holder = new Object();
				synchronized (holder) {
					method = remoteVault.getClass().getMethod(name, parametersTypes);

					System.out.print("\nSend request " + name + ":\n");
					if (parameters.length > 0) {
						System.out.println(parameters[0]);
					} else {
						System.out.println();
					}

					token = (String) method.invoke(remoteVault, parameters);
					handler.addHolder(token, holder);
					holder.wait(getTimeout());
				}
				result = handler.getResult(token);

//				System.out.println(localVault.getId() + " received: " + result);

			} catch (NoSuchMethodException | SecurityException | InterruptedException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException | ClassNotFoundException e) {
				e.printStackTrace();
				new EolRuntimeException(e.getMessage());
			}
		} //

		else if (target instanceof Vault)

		{
			Method method = null;
			try {
				Class<?>[] parametersTypes = new Class<?>[parameters.length];
				for (int i = 0; i < parameters.length; i++) {
					if (parameters[i] instanceof EolSequence) {
						parametersTypes[i] = List.class;
					} else if (parameters[i] instanceof EolMap) {
						parametersTypes[i] = Map.class;
					} else {
						parametersTypes[i] = parameters[i].getClass();
					}
				}
				Vault localVault = (Vault) target;
				method = localVault.getClass().getMethod(name, parametersTypes);
				result = method.invoke(localVault, parameters);
			} catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException
					| IllegalAccessException e) {
				new EolRuntimeException(e.getMessage());
			}
		}
		return result;
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
