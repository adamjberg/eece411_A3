package com.group7.eece411.A3;

import javax.xml.bind.DatatypeConverter;

/**
 * Various static routines to help with strings
 */
public class StringUtils { 

	public static String byteArrayToHexString(byte[] bytes) {
		return DatatypeConverter.printHexBinary(bytes);
	}

	public static byte[] hexStringToByteArray(String string) {
		return DatatypeConverter.parseHexBinary(string);
	}
}
