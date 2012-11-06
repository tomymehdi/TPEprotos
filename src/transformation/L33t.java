package transformation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class L33t {
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while ((line = br.readLine()) != null) {
			System.out.println(line);
			if (line.startsWith("Content-Type: text")) {
				while (!(line = br.readLine()).startsWith("--")) {
					char[] body = line.toCharArray();
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
					
					System.out.println(new String(body));
				}
				System.out.println(line);
			}
		}
	}

}