package pop3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MimeParser {
	
	public static void main(String[] args) throws IOException {

		String mimeFile = "mimeExamples/mimeExample.txt";
		BufferedReader br = new BufferedReader(new FileReader(mimeFile));
		String line;
		String nextLine;
		
		List<MimePart> mimeParts = new LinkedList<MimePart>();
		String aux[];
		
		boolean multipart = false;
		
		
		String version = "MIME-Version: ";
		String received = "Received: ";//puede repetirse
		String date = "Date: ";
		String messageID = "Message-ID: ";
		String subject = "Subject: ";
		String from = "From: ";
		String to = "to";
		String contentType = "Content-Type: ";
		
		//parsing the mime header
		HeaderInfo headerInfo = new HeaderInfo();
		line = br.readLine();
		while (line != null && !(multipart && headerInfo.boundary == line)) {
			
			while((nextLine = br.readLine()) != null && nextLine.length() >=1 && nextLine.charAt(0) == ' ') {
				line = line + nextLine;
			}
			
			if(line.startsWith(version)){
				headerInfo.version = line.split("MIME-Version: ")[1];
			} else if (line.startsWith(received)){
				
			} else if (line.startsWith(date)){
				headerInfo.version = line.split("Date: ")[1];
			} else if (line.startsWith(messageID)){
				
			} else if (line.startsWith(subject)){
				
			} else if (line.startsWith(from)){
				
			} else if (line.startsWith(to)){
				
			} else if (line.startsWith(contentType)){
				aux = line.split("Content-Type: ");
				aux = aux[1].split(" boundary=");
				headerInfo.type = aux[0];
				if(aux[0].startsWith("multipart")){
					multipart = true;
					headerInfo.boundary = aux[1];
				} else {
					multipart = false;
				}
				
			} else {
				//TODO validar si la opcion con la q empeiza existe y agregarla sino no hacer nada
			}
			line = nextLine;
		}
		
		//parsing the multipart if there are multiparts
		if(multipart){
			MimePart part = null;
			while((line = br.readLine()) != null){
				if(line == headerInfo.boundary){
					part = new MimePart();
					mimeParts.add(part);
				} else {
					part.body += '\n' + line;
				}
			}
		} else{
			MimePart part = new MimePart();
			part.type = headerInfo.type;
			while((line = br.readLine()) != null){
				part.body += '\n' + line;
			}
			mimeParts.add(part);
		}
	}
}
