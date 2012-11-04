package mime;

public class MimePart {

	String transferEncoding;
	String body = "";
	String type;
	String contentDisposition;
	
	public String getBody() {
		return body;
	}
	
	public void setBody(String body) {
		this.body = body;
	}

	public String getType() {
		return type;
	}

}
