package pop3.restriction;

import mime.MimeInfoSimplified;

public class HeadersRestriction implements Restriction {

	@Override
	public boolean validateRestriction(MimeInfoSimplified data) {
		return true;
	}

}
