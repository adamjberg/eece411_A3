package eece411_assg3_b;

public class ResponseData {
	public byte response_code;
	public int val_len;
	public byte [] response_value;
	
	
	
	public ResponseData(byte r_c,int v_l, byte [] r_v) {
		// constructor stub
		response_code=r_c;
		val_len = v_l;
		response_value = r_v;
			
	}
}
