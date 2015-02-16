package eece411_assg3_b;
/**
 * @author Ehsan
 *
 */
public class NodeInfo {
	byte []ipaddress; 
	int port;
	
	// Hold the range that the node is responsible for.
	public int keyfrom;
	public int keyto;

public NodeInfo (byte [] a , int p, int f, int t)
{
	ipaddress=a;
	port=p;
	keyfrom=f;
	keyto =t;
}
public NodeInfo ()
{

}

}

