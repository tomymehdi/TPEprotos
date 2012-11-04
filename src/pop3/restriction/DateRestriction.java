package pop3.restriction;

import java.util.Calendar;
import java.util.Date;


import mime.MimeInfoSimplified;

public class DateRestriction implements Restriction {

	private int days;
	
	public DateRestriction(int days) {
		this.days = days;
	}
	
	@Override
	public boolean validateRestriction(MimeInfoSimplified data) {
		Calendar c = Calendar.getInstance();
		c.roll(Calendar.DAY_OF_MONTH, -days);
		if ( c.before(data.getDate()) ) {
			return true;
		}
		return false;
	}

}
