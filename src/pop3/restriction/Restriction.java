package pop3.restriction;

import mime.MimeInfoSimplified;

public interface Restriction {
	public boolean validateRestriction(MimeInfoSimplified data);
}
