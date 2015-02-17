package eece411_assg3_b;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class ResponseData extends Protocol {

	HashMap<String, byte[]> HMdata = new HashMap<String, byte[]>();
	public static final int MAX_RESPONSE_SIZE = 15005; // 15000 + 1 + 4 bytes 
	public static final int KEY_SIZE = 32;
	public static final int VALUE_SIZE = 15000;

	public ResponseData(byte r_c, int v_l, byte[] r_v) {
		byte[] rc = new byte[1];
		rc[1] = r_c;
		HMdata.put("response_code", rc);
		HMdata.put("val_len", Conversions.int2leb(v_l, 0));
		HMdata.put("response_value", r_v);
	}

	public ResponseData() {
	}

	public String responseMaker(byte response_code, int val_len, byte[] value_in)
			throws IllegalArgumentException {
		StringBuilder sb = new StringBuilder();
		if (value_in.length > VALUE_SIZE || val_len > VALUE_SIZE) {
			throw new IllegalArgumentException();
		} else {
			sb.append(Conversions.byteArrayToHexString((HMdata
					.get("response_code"))));
			// Converts the int to little-endian array and then stringifies it
			sb.append(Conversions.byteArrayToHexString(Conversions.int2leb(
					val_len, 0)));
			sb.append(Conversions.byteArrayToHexString(value_in));
		}
		return sb.toString();
	}
	
	public byte[] ConstructGetSuccess(byte[] value) throws IOException {
		
		
		byte[] b = { 0x00 };
		HMdata.put("response_code", b);
		HMdata.put("response_value", value);
		HMdata.put("val_len", Conversions.int2leb(value.length, 0));
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		outputStream.write(HMdata.get("response_code"));
		outputStream.write(HMdata.get("val_len"));
		outputStream.write(HMdata.get("response_value"));

		return outputStream.toByteArray();
	}
	
	
	public byte [] ConstructPutSuccess() throws IOException {
		byte[] b = { 0x00 };
		HMdata.put("response_code", b);
		HMdata.put("val_len", Conversions.int2leb(0, 0));
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(HMdata.get("response_code"));
		outputStream.write(HMdata.get("val_len"));

		return outputStream.toByteArray();
	}

	public byte [] ConstructNonExistanceKey() throws IOException {
		byte[] b = { 0x01 };
		HMdata.put("response_code", b);
		HMdata.put("val_len", Conversions.int2leb(0, 0));
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(HMdata.get("response_code"));
		outputStream.write(HMdata.get("val_len"));

		return outputStream.toByteArray();
	}

	public byte [] ConstructOutOfSpace() throws IOException {
		byte[] b = { 0x02 };
		HMdata.put("response_code", b);
		HMdata.put("val_len", Conversions.int2leb(0, 0));
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(HMdata.get("response_code"));
		outputStream.write(HMdata.get("val_len"));

		return outputStream.toByteArray();
	}

	@Override
	public byte[] getHeader(String Head) {
		return HMdata.get(Head);
	}

	/* Not sure if are ever gonna use it but ... */
	public String strigyfy() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		outputStream.write(HMdata.get("response_code"));
		outputStream.write(HMdata.get("val_len"));
		outputStream.write(HMdata.get("response_value"));

		return Conversions.byteArrayToHexString(outputStream.toByteArray());
	}

	@Override
	public boolean isValidate() throws NotFoundCmdException {
		byte [] res_temp =HMdata.get("response_code"); 	
		byte res_code =  res_temp[0];
		
		byte [] val_len = HMdata.get("val_len");
		int val_len_int = Conversions.leb2int(val_len, 0);		
		byte [] val = HMdata.get("response_value");
		
		//check the validity of the  data 
		if (val.length > MAX_RESPONSE_SIZE)
		{
			throw new NotFoundCmdException("Response value is too large.");
		}
		
		
		if (!checkResponseCode(res_code))
		{
			throw new NotFoundCmdException("Invalid response code.");
		}
		
		//Check the length of val
		if (val_len_int > VALUE_SIZE || val_len_int<0 || val.length >VALUE_SIZE )
		{
			throw new NotFoundCmdException("Invalid response value length."); 
		}
		
		return true;
	}

	@Override
	public Protocol convert(byte[] d)  {
		if (d.length > MAX_RESPONSE_SIZE)
		{
			throw new IllegalArgumentException();
		}
		//Create a new instance to better preserve data.
		ResponseData rd  = new ResponseData();
		
		// Retrieve the data from the array
		byte respon_code = d[0]; // The first elemt is the Response code 		
		byte [] val_len = Arrays.copyOfRange(d, 1, 5) ; // the 1,2,3,4 the elements are the length
		int val_len_int = Conversions.leb2int(val_len, 0);
		byte [] val = Arrays.copyOfRange(d, 5, d.length);
		
		//check the validity of the retrieved data 
		if (!checkResponseCode(respon_code))
		{
			throw new IllegalArgumentException(); 
		}
		
		//Check the length of val
		if (val_len_int > VALUE_SIZE || val_len_int<0 || val.length >VALUE_SIZE )
		{
			throw new IllegalArgumentException(); 
		}
		return rd;
	}
	
	
	private static boolean checkResponseCode (byte code)
	{
		if (code == 0x00 || code == 0x01 || code == 0x02 || code == 0x03 || code == 0x04 || code == 0x05)
		{
			return true; 
		}
		else 
			return true ;
		
	}
	

}