package com.group7.eece411.A3;

import java.io.*;

/**
 * @author Ehsan This file holds useful conversion methods.
 */
public class Conversions {

	/**
	 * Various static routines to help with strings
	 */
	public static String byteArrayToHexString(byte[] bytes) {
		StringBuffer buf = new StringBuffer();
		String str;
		int val;

		for (int i = 0; i < bytes.length; i++) {
			val = ubyte2int(bytes[i]);
			str = Integer.toHexString(val);
			while (str.length() < 2)
				str = "0" + str;
			buf.append(str);
		}
		return buf.toString().toUpperCase();
	}

	
	public byte StringToByte (String str)
	{
		return Byte.parseByte(str, 16);
	}
	
	
	/*
	 * Converts a Hex string to byte array.
	 */
	public static byte[] hexStringToByteArray(String s) {
		// Source:
		// http://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * Converts a byte to Hex string
	 */
	public static String byteToHexString(byte b) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%02X ", b));
		return sb.toString();
	}

	/**
	 * Interprets the value of x as an unsigned byte, and returns it as integer.
	 * For example, ubyte2int(0xFF)==255, not -1.
	 */
	public static int ubyte2int(byte x) {
		return ((int) x) & 0x000000FF;
	}

	/**
	 * Returns the reverse of x.
	 */
	public static byte[] reverse(byte[] x) {
		int n = x.length;
		byte[] ret = new byte[n];
		for (int i = 0; i < n; i++)
			ret[i] = x[n - i - 1];
		return ret;
	}

	/**
	 * Little-endian bytes to int
	 * 
	 * @requires x.length-offset>=4
	 * @effects returns the value of x[offset..offset+4] as an int, assuming x
	 *          is interpreted as a signed little endian number (i.e., x[offset]
	 *          is LSB) If you want to interpret it as an unsigned number, call
	 *          ubytes2long on the result.
	 */
	public static int leb2int(byte[] x, int offset) {
		// Must mask value after left-shifting, since case from byte
		// to int copies most significant bit to the left!
		int x0 = x[offset] & 0x000000FF;
		int x1 = (x[offset + 1] << 8) & 0x0000FF00;
		int x2 = (x[offset + 2] << 16) & 0x00FF0000;
		int x3 = (x[offset + 3] << 24);
		return x3 | x2 | x1 | x0;
	}

	public static byte[] longToByteArray(long value) {
		return new byte[] { (byte) (value >> 56), (byte) (value >> 48),
				(byte) (value >> 40), (byte) (value >> 32),
				(byte) (value >> 24), (byte) (value >> 16),
				(byte) (value >> 8), (byte) value };
	}

	/**
	 * Little-endian bytes to int - stream version
	 * 
	 */
	public static int leb2int(InputStream is) throws IOException {
		// Must mask value after left-shifting, since case from byte
		// to int copies most significant bit to the left!
		int x0 = is.read() & 0x000000FF;
		int x1 = (is.read() << 8) & 0x0000FF00;
		int x2 = (is.read() << 16) & 0x00FF0000;
		int x3 = (is.read() << 24);
		return x3 | x2 | x1 | x0;
	}

	/**
	 * Little-endian bytes to int. Unlike leb2int(x, offset), this version can
	 * read fewer than 4 bytes. If n<4, the returned value is never negative.
	 * 
	 * @param x
	 *            the source of the bytes
	 * @param offset
	 *            the index to start reading bytes
	 * @param n
	 *            the number of bytes to read, which must be between 1 and 4,
	 *            inclusive
	 * @return the value of x[offset..offset+N] as an int, assuming x is
	 *         interpreted as an unsigned little-endian number (i.e., x[offset]
	 *         is LSB).
	 * @exception IllegalArgumentException
	 *                n is less than 1 or greater than 4
	 * @exception IndexOutOfBoundsException
	 *                offset<0 or offset+n>x.length
	 */
	public static int leb2int(byte[] x, int offset, int n)
			throws IndexOutOfBoundsException, IllegalArgumentException {
		if (n < 1 || n > 4)
			throw new IllegalArgumentException("No bytes specified");

		// Must mask value after left-shifting, since case from byte
		// to int copies most significant bit to the left!
		int x0 = x[offset] & 0x000000FF;
		int x1 = 0;
		int x2 = 0;
		int x3 = 0;
		if (n > 1) {
			x1 = (x[offset + 1] << 8) & 0x0000FF00;
			if (n > 2) {
				x2 = (x[offset + 2] << 16) & 0x00FF0000;
				if (n > 3)
					x3 = (x[offset + 3] << 24);
			}
		}
		return x3 | x2 | x1 | x0;
	}

	/**
	 * Int to little-endian bytes: writes x to buf[offset..]
	 */
	public static void int2leb(int x, byte[] buf, int offset) {
		buf[offset] = (byte) (x & 0x000000FF);
		buf[offset + 1] = (byte) ((x >> 8) & 0x000000FF);
		buf[offset + 2] = (byte) ((x >> 16) & 0x000000FF);
		buf[offset + 3] = (byte) ((x >> 24) & 0x000000FF);
	}

	/**
	 * Int to little-endian bytes: writes x to buf[offset..]
	 */
	public static byte[] int2leb(int x, int offset) {
		byte[] buf = new byte[4];
		buf[offset] = (byte) (x & 0x000000FF);
		buf[offset + 1] = (byte) ((x >> 8) & 0x000000FF);
		buf[offset + 2] = (byte) ((x >> 16) & 0x000000FF);
		buf[offset + 3] = (byte) ((x >> 24) & 0x000000FF);
		return buf;
	}

	/**
	 * long to little-endian bytes: writes x to buf[offset..]
	 */
	public static void long2leb(long x, byte[] buf, int offset) {
		buf[offset] = (byte) (x & 0x000000FF);
		buf[offset + 1] = (byte) ((x >> 8) & 0x000000FF);
		buf[offset + 2] = (byte) ((x >> 16) & 0x000000FF);
		buf[offset + 3] = (byte) ((x >> 24) & 0x000000FF);
		buf[offset + 4] = (byte) ((x >> 32) & 0x000000FF);
		buf[offset + 5] = (byte) ((x >> 40) & 0x000000FF);
		buf[offset + 6] = (byte) ((x >> 48) & 0x000000FF);
		buf[offset + 7] = (byte) ((x >> 56) & 0x000000FF);
	}

	/**
	 * short to little-endian bytes: return buf[offset..]
	 */
	public static byte[] short2leb(short x, int offset) {
		byte[] buf = new byte[2];
		buf[offset] = (byte) (x & 0x000000FF);
		buf[offset + 1] = (byte) ((x >> 8) & 0x000000FF);
		return buf;
	}

	/**
	 * Int to little-endian bytes: writes x to given stream
	 */
	public static void int2leb(int x, OutputStream os) throws IOException {
		os.write((byte) (x & 0x000000FF));
		os.write((byte) ((x >> 8) & 0x000000FF));
		os.write((byte) ((x >> 16) & 0x000000FF));
		os.write((byte) ((x >> 24) & 0x000000FF));
	}
}
