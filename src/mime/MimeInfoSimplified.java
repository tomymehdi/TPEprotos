package mime;

import java.util.LinkedList;
import java.util.List;

public class MimeInfoSimplified {
	private int mailSizeInBytes = 0;
	private String from;
	private List<AttachmentType> attachments = new LinkedList<AttachmentType>();
	public void incSize(int length) {
		mailSizeInBytes += length;
	}
	public void pushAttachment(AttachmentType attach) {
		attachments.add(attach);
	}
	public void setFrom(String from) {
		this.from = from;
	}
	
	public int getMailSizeInBytes(){
		return mailSizeInBytes;
	}
	
	public String getFrom(){
		return from;
	}
}
