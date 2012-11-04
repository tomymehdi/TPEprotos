package transformation;

import java.io.IOException;

import mime.MimeInfo;
import mime.MimeMultiPart;
import mime.MimeParser;
import mime.MimePart;

public class l337 {
	public static void main(String[] args) throws IOException {
		MimeParser mimeParser = new MimeParser();
		MimeInfo mimeInfo = mimeParser.parse(System.in);
		for (MimeMultiPart multiParts : mimeInfo.getMimeParts()) {
			for (MimePart part : multiParts.getParts()) {
				if (part.getType().contains("text")) {
					char[] body = part.getBody().toCharArray();
					int i = 0;
					boolean menorflag = false;
					for (char c : body) {
						if (c == '<') {
							menorflag = true;
						} else if (c == '>') {
							menorflag = false;
						}
						if (!menorflag) {
							switch (c) {
							case 'a':
							case 'A':
								body[i] = '4';
								break;
							case 'e':
							case 'E':
								body[i] = '3';
								break;
							case 'i':
							case 'I':
								body[i] = '1';
								break;
							case 'o':
							case 'O':
								body[i] = '0';
								break;
							}
						}
						i++;
					}
					part.setBody(String.valueOf(body));
				}
			}
		}

		for (MimeMultiPart multiParts : mimeInfo.getMimeParts()) {
			for (MimePart part : multiParts.getParts()) {
				String body = part.getBody();
				System.out.println(body);
			}
		}

	}
}
