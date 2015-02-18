package com.group7.eece411.A3;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseData extends Protocol {

	public static final int RESPONSE_SIZE_IN_BYTES = 1;
	public static final int MAX_VALUE_LENGTH = 15000;
	public static final int VALUE_LENGTH_SIZE_IN_BYTES = 2;
	public static final int MIN_MESSAGE_SIZE = RESPONSE_SIZE_IN_BYTES + VALUE_LENGTH_SIZE_IN_BYTES;
	public static final int MAX_MESSAGE_SIZE = MAX_VALUE_LENGTH	+ MIN_MESSAGE_SIZE;

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
	private Header header;
	private ConcurrentHashMap<String, byte[]> HMdata;
	
	public ResponseData(Header h, int res, byte[] value) {
		this.header = h;
		HMdata = new ConcurrentHashMap<String, byte[]>();		
		HMdata.put("response", new byte[]{(byte) res});
		HMdata.put("value-length", ByteOrder.int2leb(value.length));
		HMdata.put("value", value);
	}
	
	public ResponseData(ResponseCode responseCode, String value) {
		this.responseCode = responseCode;
		this.value = value;
	}

	// TODO: This should be verified for correctness
	@Override
	public Protocol fromBytes(byte[] d) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(d).order(
				java.nio.ByteOrder.LITTLE_ENDIAN);

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

	@Override
	public byte[] toBytes() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(MIN_MESSAGE_SIZE + HMdata.get("value").length)
				.order(java.nio.ByteOrder.LITTLE_ENDIAN);
		byteBuffer.put(HMdata.get("response"));
		byteBuffer.put(HMdata.get("value-length")); 
		if(HMdata.get("value") != null) {
			byteBuffer.put(HMdata.get("value")); 
		}
		return byteBuffer.array();
	}

	@Override
	public byte[] getRawHeader(String Head) {
		return HMdata.get(Head);
	}

	@Override
	public Integer getHeaderCode(String Head) {
		byte[] bytes = HMdata.get(Head);
		if(Head.equals("response") || Head.equals("value-length")) {
			if(bytes.length >= 4) {
				return ByteOrder.leb2int(bytes, 0);
			}
			return ByteOrder.leb2int(bytes, 0, bytes.length);
		} 
		return -1;
	}

	@Override
	public byte[] getUniqueId() {
		return this.header.getUniqueId();
	}

	@Override
	public Header getHeader() {
		return this.header;
	}
}