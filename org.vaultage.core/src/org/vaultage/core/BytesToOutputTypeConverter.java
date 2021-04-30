package org.vaultage.core;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class BytesToOutputTypeConverter {

	protected Object output;
	protected Class<?> outputType; 
	
	public BytesToOutputTypeConverter(Class<?> outputType) {
		this.outputType = outputType;
	}

	public Object convert(ByteArrayOutputStream outputStream) {
		byte[] bytes = outputStream.toByteArray();
		if (outputType.equals(Integer.class)) {
			ByteBuffer wrapped = ByteBuffer.wrap(bytes); 
			output = wrapped.getInt();
		} else if (outputType.equals(String.class)) {
			output = new String(bytes);
		} else if (outputType.equals(Double.class)) {
			ByteBuffer wrapped = ByteBuffer.wrap(bytes); 
			output = wrapped.getDouble();
		} else {
			output = customConvert(outputStream);
		}
		return output;
	}
	
	protected  Object customConvert(ByteArrayOutputStream outputStream) {
		return null;
	}
}
