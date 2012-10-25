package pop3;

import java.util.LinkedList;
import java.util.List;

public class User {
	
	private String URI;
	private List<Restriction> restrictions = new LinkedList<Restriction>();

	public User(String URI) {
		this.URI = URI;
	}

}
