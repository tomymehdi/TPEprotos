package pop3;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Session {
	
	private static final int BUFFER_SIZE = 1024;
	
	private  ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	private SelectionKey key;
	private SocketChannel channel;
	private boolean fromServer;
	private State state;
	private String digest;
	
	public Session(SocketChannel channel, boolean fromServer) {
		this(null,channel,fromServer);
	}
	public Session(SelectionKey key,
			SocketChannel channel, boolean fromServer) {
		this.key = key;
		this.channel = channel;
		this.fromServer = fromServer;
		this.digest = "" + System.currentTimeMillis();
		this.buffer.put(new String("+OK POP3 server Aloha <" + digest + ">").getBytes());
		
		
	}
	public ByteBuffer getBuffer() {
		return buffer;
	}
	public void setBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}
	public SelectionKey getKey() {
		return key;
	}
	public void setKey(SelectionKey key) {
		this.key = key;
	}
	public SocketChannel getChannel() {
		return channel;
	}
	public void setChannel(SocketChannel channel) {
		this.channel = channel;
	}
	public boolean isFromServer() {
		return fromServer;
	}
	public void setFromServer(boolean fromServer) {
		this.fromServer = fromServer;
	}
	
	public String getDigest(){
		return digest;
	}
	

	public enum State{
		AUTH, TRANS, UPDATE;
	}
}


