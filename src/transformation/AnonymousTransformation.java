package transformation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AnonymousTransformation {
	
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line;
		boolean done = false;
		while ((line = br.readLine()) != null) {
			if ( !line.startsWith("From") || done ) {
				System.out.println(line);
			} else {
				done = true;
			}
		}
	}

}
