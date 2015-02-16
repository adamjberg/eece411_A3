/**
 * 
 */
package com.group7.eece411.A3;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Ehsan
 * 
 */
public class RequestData extends Protocol {

	public static final int COMMAND_SIZE_IN_BYTES = 1;
	public static final int MAX_VALUE_LENGTH = 15000;
	public static final int KEY_SIZE_IN_BYTES = 32;
	public static final int MIN_MESSAGE_SIZE = KEY_SIZE_IN_BYTES + COMMAND_SIZE_IN_BYTES;

	public enum RequestCommand {
		INVALID(0x00), PUT(0x01), GET(0x02), REMOVE(0x03), SHUTDOWN(0x04);
		private int value;

		RequestCommand(int code) {
			this.value = code;
		}
	}

	public Header header;
	public RequestCommand requestCommand;
	public String key;
	public String value;

	public RequestData(char requestCommand, String key, String value) throws NotFoundCmdException {
		this();
		this.key = key;
		this.value = value;
		if(requestCommand < RequestCommand.values().length)
		{
			this.requestCommand = RequestCommand.values()[requestCommand];
		}
		else
		{
			throw new NotFoundCmdException("Command " + requestCommand + " not found");
		}
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
	public Protocol fromBytes(byte[] inBytes) throws UnknownHostException, NotFoundCmdException {
		
		byte[] headerBytes = Arrays.copyOfRange(inBytes, 0, Header.LENGTH_IN_BYTES);
		header.decode(headerBytes);
		
		byte[] messageBytes = Arrays.copyOfRange(inBytes, Header.LENGTH_IN_BYTES, inBytes.length);
		
		if(messageBytes.length <= MIN_MESSAGE_SIZE)
		{
			return null;
		}
		System.out.println("length " + messageBytes.length);
		ByteBuffer byteBuffer = ByteBuffer.wrap(messageBytes).order(
				ByteOrder.LITTLE_ENDIAN);

		char requestCommand = byteBuffer.getChar();
		System.out.println("Req " + requestCommand);
		byte[] keyBuffer = new byte[KEY_SIZE_IN_BYTES];
		byteBuffer.get(keyBuffer);
		String key = StringUtils.byteArrayToHexString(keyBuffer);
		System.out.println("KEY " + key);
		String value = "";
		if(byteBuffer.remaining() >= 2)
		{
			int valueLength = byteBuffer.getShort();

			if (valueLength > 0 && valueLength < MAX_VALUE_LENGTH
					&& valueLength < byteBuffer.remaining()) {
				byte[] valueBuffer = new byte[valueLength];
				byteBuffer.get(valueBuffer);
				value = StringUtils.byteArrayToHexString(valueBuffer);
				System.out.println("value " + value);
			} else {
				System.out.println("Invalid Value, using empty string");
			}
		}

		return new RequestData(requestCommand, key,
				value);
	}

	@Override
	public byte[] toBytes() {
		// TODO Auto-generated method stub
		return null;
	}
}
