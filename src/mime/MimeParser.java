package mime;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

public class MimeParser {
	
	//public static void main(String[] args) throws IOException {
	//	BufferedReader bb = new BufferedReader(new StringReader("lalalalalal" + '\n' + "caomidajdflkasd"));
	//	String l = "";
	//	while((l = bb.readLine())!=null){
	//		System.out.println(l);
	//	}
	//}
	
	private static String version = "MIME-Version: ";
	private static String received = "Received: ";//puede repetirse
	private static String date = "Date: ";
	private static String messageID = "Message-ID: ";
	private static String subject = "Subject: ";
	private static String from = "From: ";
	private static String to = "To: ";
	private static String contentType = "Content-Type: ";
	private static String contentTransferEncoding = "Content-Transfer-Encoding: ";
	private static String contentDisposition = "Content-Disposition: ";
	private static String attachmentId = "X-Attachment-Id: ";
	
	public static void main(String[] args) {
		String mail = "";
		String line = "";
		
		try {
			BufferedReader br = new BufferedReader(new FileReader("mimeExamples/mimeExample.txt"));
			while((line=br.readLine())!=null){
				mail += line + '\n';
			}
			MimeInfo mimeInfo = MimeParser.parse(mail);
			System.out.println(mimeInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static MimeInfo parse(String mimeString) throws IOException {

		BufferedReader br = new BufferedReader(new StringReader(mimeString));
		String line;
		String nextLine;
		
		HeaderInfo headerInfo = new HeaderInfo();
		List<MimeMultiPart> mimeParts = new LinkedList<MimeMultiPart>();
		MimeInfo resp = new MimeInfo(headerInfo, mimeParts);
		String aux[];
		
		//parsing the mime header
		line = br.readLine();
		while (line != null ) {
			if(!line.isEmpty()){
				while((nextLine = br.readLine()) != null && nextLine.length() >=1 && nextLine.charAt(0) == ' ') {
					line = line + nextLine;
				}
				
				if(line.startsWith(version)){
					headerInfo.version = line.split("MIME-Version: ")[1];
				} else if (line.startsWith(received)){
					
				} else if (line.startsWith(date)){
					headerInfo.date = line.split("Date: ")[1];
				} else if (line.startsWith(messageID)){
					
				} else if (line.startsWith(subject)){
					
				} else if (line.startsWith(from)){
					
				} else if (line.startsWith(to)){
					
				} else if (line.startsWith(contentType)){
					aux = line.split("Content-Type: ");
					aux = aux[1].split(" boundary=");
					if(aux[0].startsWith("multipart")){
						String type = aux[0].replaceAll(";", "");
						String boundary = aux[1];
						parseMultipart(mimeParts, type, boundary, br);
					}
					
				} else {
					//TODO validar si la opcion con la q empeiza existe y agregarla sino no hacer nada
				}
				line = nextLine;
			} else {
				line = br.readLine();
			}
		}
		return resp;
	}

	private static void parseMultipart(List<MimeMultiPart> mimeParts, String type, String boundary, BufferedReader br) throws IOException {
		//parsing the multipart if there are multiparts
		String line;
		String nextLine;
		String aux[];
		MimeMultiPart multiPart = new MimeMultiPart();
		mimeParts.add(multiPart);
		multiPart.type = type;
		multiPart.boundary = boundary;
		MimePart part = new MimePart();
		line = br.readLine();
		if(!line.equals("--" + boundary)){
			throw new RuntimeException();
		}
		while(line != null){
			if(!line.isEmpty()){
							
				while((nextLine = br.readLine()) != null && !nextLine.isEmpty() && nextLine.charAt(0) == ' ') {
					line = line + nextLine;
				}
				if(line.equals( "--" + boundary + "--")){
					return;
				} else if(line.equals( "--" + boundary)){
					part = new MimePart();
					multiPart.parts.add(part);
				} else {
					if (line.startsWith(contentType)){
						aux = line.split(contentType);
						aux = aux[1].split(" boundary=");
						if(aux[0].startsWith("multipart")){
							String typeR = aux[0].replaceAll(";", "");
							String boundaryR = aux[1];
							parseMultipart(mimeParts, typeR, boundaryR, br);
						} else {
							part.type = aux[0].replaceAll(";","");
						}
					} else if(line.startsWith(contentTransferEncoding)){
						aux = line.split(contentTransferEncoding);
						part.transferEncoding = aux[1].replaceAll(";", "");
					} else if(line.startsWith(contentDisposition)) {
						aux = line.split(contentDisposition);
						part.contentDisposition = aux[1].replaceAll(";", "");
					} else if(line.startsWith(attachmentId)){
						aux = line.split(attachmentId);
						part.contentDisposition = aux[1].replaceAll(";", "");
					} else {
						part.body += line + '\n';
					}
				}
				line = nextLine;
			} else {
				line = br.readLine();
			}
		}
		
	}
}
