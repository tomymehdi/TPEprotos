package pop3;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.LinkedList;
import java.util.Queue;

public class TransformThread extends Thread {

	private Queue<ByteBuffer> bufferQueue = new LinkedList<ByteBuffer>();
	private String path;
	private SelectionKey key;
	private boolean finished;

	public TransformThread(String path, SelectionKey key) {
		this.path = path;
		this.key = key;
	}

	@Override
	public void run() {
		Process p;
		try {
			
			ProcessBuilder builder = new ProcessBuilder();
			builder.redirectErrorStream(true); // This is the important part
			builder.command(path);
			p = builder.start();
			Reader programOutput = new InputStreamReader(
					p.getInputStream());
			Writer programInput = new OutputStreamWriter(
					p.getOutputStream());
			while (!Thread.interrupted()) {
				try {
					p.exitValue();
					if ( !programOutput.ready() ) {
						programOutput.close();
						interrupt();
						return;
					}
				} catch( IllegalThreadStateException e ) {
					
				}
				while ( programOutput.ready() ) {
					char[] charBuffer = new char[Session.BUFFER_SIZE];
					programOutput.read(charBuffer);
					Session session = (Session) key.attachment();
					session.addToBuffer(new String(charBuffer).getBytes());
				}
				ByteBuffer b;
				if ( bufferQueue.isEmpty() ) {
					if ( finished ) {
						programInput.close();
					} 
					continue;
				}
				b = bufferQueue.poll();
				programInput.write(new String(b.array())
				.substring(b.position(),
						b.limit()));
				programInput.flush();
			}
		} catch (IOException e) {
			System.out.println("Error transforming!");
		}

	}
	
	public void addBuffer(ByteBuffer b) {
		bufferQueue.add(b);
	}

	public void finished() {
		finished = true;
	}
	
}
