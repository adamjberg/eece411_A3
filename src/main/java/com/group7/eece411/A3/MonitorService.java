package com.group7.eece411.A3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * 
 *
 */
public class MonitorService extends Service
{	
	public static final String host = "54.68.197.12";
	public static final int port = 41170;
	private JSONObject loc;
	
	public MonitorService(int period) throws IOException {
		super(period, 41171);
		try {
			this.loc = (JSONObject)(new JSONParser()).parse(request("http://ip-api.com/json/"+InetAddress.getLocalHost().getHostAddress(), "GET"));
		} catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
			Datastore.getInstance().addException("JSON ParseException", e);
			this.loc = new JSONObject();
		} 
	}
	
    public void run()
    {	
		try {
			Datastore.getInstance().findThisNode().update();
			client.send(host, port, getData());
		} catch (IOException e) {
			e.printStackTrace();
			Datastore.getInstance().addException("EXCEPTION", e);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
    }
    
    public String request(String url, String method) throws MalformedURLException, IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestMethod(method);
		if(connection.getResponseCode() == 200) {
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			return in.readLine();
		}
		return null;
	}
    
	@SuppressWarnings("unchecked")
	public byte[] getData() throws IOException, ParseException, org.json.simple.parser.ParseException {
		JSONObject map=new JSONObject();
		map.put("hostname", SystemCmd.getHostName());
		map.put("systemUptime", SystemCmd.uptime());
		map.put("spaceAvailable", SystemCmd.getDiskAvailableSize());
		map.put("averageLoads", SystemCmd.getLoad());
		map.put("serviceUptime", ManagementFactory.getRuntimeMXBean().getUptime());
		map.put("loc", this.loc);
		map.put("logs", Datastore.getInstance().getLogs());
		map.put("kvstore", Datastore.getInstance().findAll());	
		map.put("index", Datastore.getInstance().findThisNode().getLocation());

		return map.toJSONString().getBytes();
    }
}
