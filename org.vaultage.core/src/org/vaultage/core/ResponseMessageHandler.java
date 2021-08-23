package org.vaultage.core;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;

public abstract class ResponseMessageHandler {

	public abstract void process(VaultageMessage message, String senderPublicKey, Vault vault) throws Exception;

	protected Object[] parseType(String returnValueClassName) throws ClassNotFoundException {
		int ltIndex = returnValueClassName.indexOf("<");
		String firstSegment = null;
		String secondSegment = null;
		if (ltIndex > -1) {
			firstSegment = returnValueClassName.substring(0, ltIndex);
			secondSegment = returnValueClassName.substring(ltIndex + 1, returnValueClassName.length() - 1);
		}

		if (firstSegment != null && secondSegment != null) {
			Class<?> returnClass = Class.forName(firstSegment);
			String[] types = secondSegment.split(",");
			Type[] typeArguments = new Type[types.length];
			for (int i = 0; i < types.length; i++) {
				Object[] temp = parseType(types[i]);
				typeArguments[i] = (Type) temp[1];
			}

			Type parameterisedType = TypeToken.getParameterized(returnClass, typeArguments).getType();
			return new Object[] { returnClass, parameterisedType };
		} else {
			Class<?> returnClass = Class.forName(returnValueClassName);
			Type returnType = (Type) returnClass;
			return new Object[] { returnClass, returnType };
		}
	}

}
