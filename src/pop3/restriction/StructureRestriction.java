package pop3.restriction;

import mime.MimeInfoSimplified;

public class StructureRestriction implements Restriction {

	private boolean attachments;
	
	public StructureRestriction(String condition) {
		if ( condition.equals("attachments") ) {
			attachments = true;
		} else if ( condition.equals("no attachments") ) {
			attachments = false;
		}
	}
	
	@Override
	public boolean validateRestriction(MimeInfoSimplified data) {
		if ( (data.getAttachments().size() > 0 && attachments) || (data.getAttachments().size() == 0 && !attachments)) {
			return false;
		}
		return true;
	}
	

}
