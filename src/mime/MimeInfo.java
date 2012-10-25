package mime;

import java.util.List;

public class MimeInfo {
	private HeaderInfo headerInfo;
	private List<MimeMultiPart> mimeParts;
	
	public MimeInfo(HeaderInfo headerInfo, List<MimeMultiPart> mimeParts) {
		this.headerInfo = headerInfo;
		this.mimeParts = mimeParts;
	}

	public List<MimeMultiPart> getMimeParts() {
		return mimeParts;
	}

	public HeaderInfo getHeaderInfo() {
		return headerInfo;
	}


	
	

}
