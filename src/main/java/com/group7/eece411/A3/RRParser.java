package eece411_assg3_b;
/**
 * @author Ehsan
 *
 */
public class RRParser {
	//Constants 
		public static final int KEY_SIZE= 32;
		public static final int VALUE_SIZE = 15000;
		
		
		/*
		 * I have this class so that we can use as a response packet storage for response messages. 
		 * Response packet could be saved in this class after it gets unmarshaled.
		 * */
		public class responseData {
			public byte response_code;
			public int val_len;
			public byte [] response_value;
			public responseData(byte r_c,int v_l, byte [] r_v) {
				// constructor stub
				response_code=r_c;
				val_len = v_l;
				response_value = r_v;
					
			}
			
		}
		
		
		public class RequestData {
			public byte command ;
			public byte [] key;
			public int val_len;
			public byte [] response_value;
			public RequestData(byte c , byte [] k,int v_l, byte [] r_v) {
				// constructor stub
				command=c;
				key=k;
				val_len = v_l;
				response_value = r_v;
					
			}
			
		}
		
		
		public RRParser() {
			// Nothing to allocate
		}

		
		
		public String commandMaker (byte command_in, byte[] key_in,int val_len, byte[] value_in ) throws IllegalArgumentException
		{
			StringBuilder sb = new StringBuilder();
			if (value_in.length > VALUE_SIZE || val_len>VALUE_SIZE)
			{
				throw new IllegalArgumentException();
			}
			else 
			{
				sb.append(Conversions.byteToHexString(command_in));
				sb.append(Conversions.byteArrayToHexString(key_in));
				sb.append(String.valueOf(val_len));
				sb.append(Conversions.byteArrayToHexString(value_in));
			}
			
			return sb.toString();
		}
		
		
		public String responseMaker (byte response_code,int val_len, byte[] value_in  )throws IllegalArgumentException
		{
			StringBuilder sb = new StringBuilder();
			if (value_in.length > VALUE_SIZE || val_len>VALUE_SIZE)
			{
				throw new IllegalArgumentException();
			}
			else 
			{
				
				sb.append(Conversions.byteToHexString(response_code));
				// Converts the int to little-endian array and then stringifies it 
				sb.append(Conversions.byteArrayToHexString(Conversions.int2leb(val_len, 0)));
				sb.append(Conversions.byteArrayToHexString(value_in));
			}
			
			return sb.toString();
		}
}
