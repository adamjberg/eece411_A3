package com.group7.eece411.A3;

import java.net.UnknownHostException;

public class SyncService extends Service {

	public SyncService(int period) throws UnknownHostException {
		super(period);
	}

	public void run() {
		NodeInfo target = Datastore.getInstance().findRandomNode();
		Datastore.getInstance().addLog("INFO", "Sync with "+target.getHost());
	}
}
