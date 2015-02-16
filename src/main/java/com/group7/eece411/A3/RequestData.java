/**
 * 
 */
package com.group7.eece411.A3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Ehsan
 * 
 */
public class RequestData extends Protocol {

	public static final int MAX_VALUE_LENGTH = 15000;
	public static final int KEY_SIZE = 32;

	public enum RequestCommand {
		INVALID(0x00), PUT(0x01), GET(0x02), REMOVE(0x03), SHUTDOWN(0x04);
		private int value;

		RequestCommand(int code) {
			this.value = code;
		}
	}

	public RequestCommand RequestCommand;
	public String key;
	public String value;

	public RequestData(RequestCommand RequestCommand, String key, String value) {
		this.RequestCommand = RequestCommand;
		this.key = key;
		this.value = value;
	}

	public RequestData() {
	}

	@Override
	public Protocol fromBytes(byte[] d) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(d).order(
				ByteOrder.LITTLE_ENDIAN);

		char requestCommand = byteBuffer.getChar();
		byte[] keyBuffer = new byte[KEY_SIZE];
		byteBuffer.get(keyBuffer);
		String key = StringUtils.byteArrayToHexString(keyBuffer);
		int valueLength = byteBuffer.getShort();

		String value = "";

		if (valueLength > 0 && valueLength < MAX_VALUE_LENGTH
				&& valueLength < byteBuffer.remaining()) {
			byte[] valueBuffer = new byte[valueLength];
			byteBuffer.get(valueBuffer);
			value = StringUtils.byteArrayToHexString(valueBuffer);
		} else {
			System.out.println("Invalid Value, using empty string");
		}

		return new RequestData(RequestCommand.values()[requestCommand], key,
				value);
	}

	@Override
	public byte[] toBytes() {
		// TODO Auto-generated method stub
		return null;
	}
}
