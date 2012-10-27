package pop3;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Session {

	private static final int BUFFER_SIZE = 1024;

	private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	private SelectionKey key;
	private SocketChannel channel;
	private boolean fromServer;
	private State state = State.AUTH;
	private String digest;
	private CharBuffer auxBuffer = CharBuffer.allocate(BUFFER_SIZE);

	public Session(SocketChannel channel, boolean fromServer) {
		this(null, channel, fromServer);
	}

	public Session(SelectionKey key, SocketChannel channel, boolean fromServer) {
		this.key = key;
		this.channel = channel;
		this.fromServer = fromServer;
		this.digest = "" + System.currentTimeMillis();
		if (!isFromServer()) {
			this.buffer.put(new String("+OK POP3 server Aloha <" + digest + ">\n").getBytes());
		}
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

	public String getDigest() {
		return digest;
	}
	
	public void setDigest(String digest) {
		this.digest = digest;
	}

	public State getState() {
		return state;
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
	public enum State {
		AUTH, TRANS, UPDATE, FIRST, PASS;
	}
	
	public class Command {
		
		private String commandName;
		private String command;
		
		Command(String commandName, String command){
			this.commandName = commandName;
			this.command = command;
		}
		
		public String getCommand() {
			return command;
		}
		
		public String getCommandName() {
			return commandName;
		}
	}

	public Command processUser(ByteBuffer buffer) {
		buffer.flip();
		while(buffer.hasRemaining()){
			byte b = buffer.get();
			if ( b=='\n'){
				auxBuffer.flip();
				Command command = processCommand(auxBuffer.toString());
				auxBuffer.compact();
				buffer.compact();
				auxBuffer.clear();
				return command;
			}
			auxBuffer.put((char) b);
		}
		buffer.compact();
		return null;
	}

	private Command processCommand(String string) {
		if( State.AUTH.equals(state) ){
			if (string.toLowerCase().startsWith("user")){
				String[] split = string.split(" ");
				if ( split.length < 2 ) {
					return null;
				}
				return new Command("user", string);
				
			} else {
				if (string.toLowerCase().startsWith("pass")){
					return new Command("pass", string);
					
				}
			}
		} else {
			String[] split = string.split(" ");
			System.out.println("-----" +split[0]);
			return new Command(split[0], string);
		}
		return null;
		
	}
}
