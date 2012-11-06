package pop3.restriction;

import mime.MimeInfoSimplified;

public class SizeRestriction implements Restriction {

	private int size;
	
	public SizeRestriction(int size) {
		this.size = size;
	}

	@Override
	public boolean validateRestriction(MimeInfoSimplified data) {
		if ( data.getMailSizeInBytes() <= size ) {
			return false;
		}
		return true;
	}
	
	
	
}
