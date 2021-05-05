package org.vaultage.core;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/***
 * Class that is responsible to convert bytes to certain built-in/custom data types/structures.
 * Incoming streams is received as byte[]. This should be conveted to certain data types before returned to users.
 * For example, streamed string data is received as bytes (byte[]). This class then converts it to String. 
 * 
 * @author Alfa Yohannis
 *
 */
public class BytesToOutputTypeConverter {

	protected Object output;
	protected Class<?> outputType;

	public BytesToOutputTypeConverter(Class<?> outputType) {
		this.outputType = outputType;
	}

	public Object convert(ByteArrayOutputStream outputStream) {
		byte[] bytes = outputStream.toByteArray();
		output = convert(bytes);
		return output;
	}

	/***
	 * The default conversion method from bytes to certain Java built-in types. More
	 * Java built-in data types and data structures should be added here for future work.
	 * 
	 * @param bytes
	 * @return
	 */
	public Object convert(byte[] bytes) {
		if (outputType.equals(Integer.class)) {
			ByteBuffer wrapped = ByteBuffer.wrap(bytes);
			output = wrapped.getInt();
		} else if (outputType.equals(String.class)) {
			output = new String(bytes);
		} else if (outputType.equals(Double.class)) {
			ByteBuffer wrapped = ByteBuffer.wrap(bytes);
			output = wrapped.getDouble();
		} else {
			output = customConvert(bytes);
		}
		return output;
	}

	/***
	 * Override this method for applying custom coversion from bytes to a certain
	 * type.
	 * 
	 * @param bytes
	 * @return
	 */
	protected Object customConvert(byte[] bytes) {
		return bytes;
	}

}
