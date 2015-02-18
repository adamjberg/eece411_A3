package com.group7.eece411.A3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * This class is used to construct Response messages.
 * */
public class ResponseData extends Protocol {

	public static final int KEY_SIZE = 32;
	public static final int MAX_VALUE_LENGTH = 15000;

	public enum ResponseCode {
		SUCCESS(0x00), INVALID_KEY(0x01), OUT_OF_SPACE(0x02), SYSTEM_OVERLOAD(
				0x03), KVSTORE_FAILURE(0x04), INVALID_COMMAND(0x05);
		private int value;

		ResponseCode(int code) {
			this.value = code;
		}
	}

	public ResponseCode responseCode;
	public String value;

	public ResponseData(ResponseCode responseCode, String value) {
		this.responseCode = responseCode;
		this.value = value;
	}

	// TODO: This should be verified for correctness
	@Override
	public Protocol fromBytes(byte[] d) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(d).order(
				ByteOrder.LITTLE_ENDIAN);
		
		char responseCode = byteBuffer.getChar();
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
		return new ResponseData(ResponseCode.values()[responseCode], value);
	}

	// TODO: This function needs to be implemented
	@Override
	public byte[] toBytes() {
		return null;
	}

	@Override
	public byte[] getRawHeader(String Head) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getHeaderCode(String Head) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getUniqueId() {
		// TODO Auto-generated method stub
		return null;
	}

}