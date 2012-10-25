package mime;

import java.util.LinkedList;
import java.util.List;

public class MimeMultiPart {

	String type;
	String boundary;
	List<MimePart> parts = new LinkedList<MimePart>();
}
