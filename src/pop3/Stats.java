package pop3;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class Stats {

	private Set<Calendar> accessLog;
	private int timesAccessed;
	private int emailsRead;
	private int emailsDeleted;
	private int bytesTransferred;

	public void setBytesTransfered(int bytesTransfered) {
		this.bytesTransferred = bytesTransfered;
	}

	public void setEmailsDeleted(int emailsDeleted) {
		this.emailsDeleted = emailsDeleted;
	}

	public void setEmailsRead(int emailsRead) {
		this.emailsRead = emailsRead;
	}

	public void emailRead() {
		emailsRead++;
	}

	public void emailDeleted() {
		emailsDeleted++;
	}

	public void bytesTansferred(int bytes) {
		bytesTransferred += bytes;
	}

	public void loggedIn() {
		if ( accessLog == null ) {
			accessLog = new HashSet<Calendar>();
		}
		accessLog.add(Calendar.getInstance());
		timesAccessed++;
	}

	public Set<Calendar> getAccessLog() {
		return accessLog;
	}

	public int getBytesTransfered() {
		return bytesTransferred;
	}

	public int getEmailsDeleted() {
		return emailsDeleted;
	}

	public int getEmailsRead() {
		return emailsRead;
	}
	
	public int getTimesAccessed() {
		return timesAccessed;
	}
	
	public void setTimesAccessed(int timesAccessed) {
		if ( accessLog != null ) {
			throw new IllegalStateException();
		}
		this.timesAccessed = timesAccessed;
	}

}
