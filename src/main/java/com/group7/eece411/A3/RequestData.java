/**
 * 
 */
package com.group7.eece411.A3;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Ehsan
 * 
 */
public class RequestData extends Protocol {

	public static final int COMMAND_SIZE_IN_BYTES = 1;
	public static final int MAX_VALUE_LENGTH = 15000;
	public static final int KEY_SIZE_IN_BYTES = 32;
	public static final int VALUE_LENGTH_SIZE_IN_BYTES = 2;
	public static final int MIN_MESSAGE_SIZE = KEY_SIZE_IN_BYTES
			+ COMMAND_SIZE_IN_BYTES;
	public static final int MAX_MESSAGE_SIZE = MAX_VALUE_LENGTH
			+ MIN_MESSAGE_SIZE;

	public enum RequestCommand {
		INVALID(0x00), PUT(0x01), GET(0x02), REMOVE(0x03), SHUTDOWN(0x04);
		private byte value;

		RequestCommand(int code) {
			this.value = (byte) code;
		}
	}

	public Header header;
	public RequestCommand requestCommand;
	public String key;
	public String value;

	public RequestData(byte requestCommand, String key, String value)
			throws NotFoundCmdException {
		this();
		this.key = key;
		this.value = value;
		if (requestCommand < RequestCommand.values().length) {
			this.requestCommand = RequestCommand.values()[requestCommand];
		} else {
			throw new NotFoundCmdException("Command " + requestCommand
					+ " not found");
		}
	}

	public RequestData(RequestCommand requestCommand, String key) {
		this(requestCommand, key, "");
	}

	public RequestData(RequestCommand requestCommand, String key, String value) {
		this();
		this.requestCommand = requestCommand;
		this.key = key;
		this.value = value;
	}

	public RequestData() {
		this.requestCommand = RequestCommand.INVALID;
		this.key = "";
		this.value = "";
		this.header = new Header();
	}

	@Override
	public Protocol fromBytes(byte[] inBytes) throws UnknownHostException,
			NotFoundCmdException {

		byte[] headerBytes = Arrays.copyOfRange(inBytes, 0,
				Header.LENGTH_IN_BYTES);
		header.decode(headerBytes);

		byte[] messageBytes = Arrays.copyOfRange(inBytes,
				Header.LENGTH_IN_BYTES, inBytes.length);

		if (messageBytes.length < MIN_MESSAGE_SIZE) {
			return null;
		}
		ByteBuffer byteBuffer = ByteBuffer.wrap(messageBytes).order(
				java.nio.ByteOrder.LITTLE_ENDIAN);

		byte requestCommand = byteBuffer.get();
		byte[] keyBuffer = new byte[KEY_SIZE_IN_BYTES];
		byteBuffer.get(keyBuffer);
		String key = StringUtils.byteArrayToHexString(keyBuffer);
		String value = "";
		if (byteBuffer.remaining() >= 2) {
			int valueLength = byteBuffer.getShort();

			if (valueLength > 0 && valueLength < MAX_VALUE_LENGTH
					&& valueLength <= byteBuffer.remaining()) {
				byte[] valueBuffer = new byte[valueLength];
				byteBuffer.get(valueBuffer);
				value = StringUtils.byteArrayToHexString(valueBuffer);
			}
		}

		return new RequestData(requestCommand, key, value);
	}

	@Override
	public byte[] toBytes() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(getMessageSizeInBytes())
				.order(java.nio.ByteOrder.LITTLE_ENDIAN);
		byteBuffer.put(requestCommand.value);
		byteBuffer.put(Arrays.copyOf(StringUtils.hexStringToByteArray(key),KEY_SIZE_IN_BYTES));
		if (value.length() > 0) {
			byteBuffer.putShort((short) value.length());
			byteBuffer.put(value.getBytes());
		}

		return byteBuffer.array();
	}

	private int getMessageSizeInBytes() {
		if (value.length() > 0) {
			return MIN_MESSAGE_SIZE + VALUE_LENGTH_SIZE_IN_BYTES
					+ value.length();
		} else {
			return MIN_MESSAGE_SIZE;
		}
	}
}
