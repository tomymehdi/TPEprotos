package pop3;

import java.util.LinkedList;
import java.util.List;

public class User {

	private String server;
	private List<Restriction> restrictions = new LinkedList<Restriction>();

	public User(String server) {
		this.server = server;
	}

	public String getServer() {
		return server;
	}
}
