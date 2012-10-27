package pop3;

import java.util.LinkedList;
import java.util.List;

public class User {

	private String server;
	private List<Restriction> restrictions = new LinkedList<Restriction>();
	private int connectionCount;
	private int maxConnections = -1;

	public User(String server) {
		this.server = server;
	}

	public String getServer() {
		return server;
	}
	
	public boolean connect() {
		if ( maxConnections != -1) {
			if ( connectionCount == maxConnections ) {
				return false;
			}
			connectionCount++;
		}
		return true;
	}
	
	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}
	
}
