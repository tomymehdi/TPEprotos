package pop3.restriction;

import mime.MimeInfoSimplified;

public class FromRestriction implements Restriction {

	String from;
	boolean exactMatch;

	public FromRestriction(String pattern, boolean exactMatch) {
		from = pattern;
		this.exactMatch = exactMatch;
	}

	@Override
	public boolean validateRestriction(MimeInfoSimplified data) {
		if (exactMatch) {
			if (data.getFrom().equals(from)) {
				return true;
			}
		} else if (data.getFrom().contains(from)) {
			return true;
		}
		return false;
	}

}
