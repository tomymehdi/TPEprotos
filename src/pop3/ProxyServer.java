package pop3;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class ProxyServer {

	private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)
	private static final String DEFAULT_USER_URL = "pop3.alu.itba.edu.ar";
	private Map<String,Transformation>transformations = new HashMap<String,Transformation>();
	private RstrictedIp restrictedIps;
	private List<User> users = new LinkedList<User>();
	private User defaultUser = new User(DEFAULT_USER_URL);
	// TODO revisar la clase AeSimpleMD5 para ver como se usa Message diggest....

	public static void main(String[] args) throws IOException {
		Selector selector = Selector.open();
		ServerSocketChannel listnChannel = ServerSocketChannel.open();

		listnChannel.socket().bind(new InetSocketAddress("localhost", 3000));
		listnChannel.configureBlocking(false);
		listnChannel.register(selector, SelectionKey.OP_ACCEPT);
		while (true) {
			if (selector.select(TIMEOUT) == 0) {
				System.out.print(".");
				continue;
			}
			Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
			while (keyIter.hasNext()) {
				SelectionKey key = keyIter.next();
				if (key.isValid() && key.isAcceptable()) {
					/*SocketChannel senderSocket = SocketChannel
							.open(new InetSocketAddress("pop3.alu.itba.edu.ar",	110));
					senderSocket.configureBlocking(false);
					SelectionKey senderSocketKey = senderSocket.register(
							selector, SelectionKey.OP_READ);
					SocketChannel clntChan = ((ServerSocketChannel) key
							.channel()).accept();
					clntChan.configureBlocking(false);
					
					Session session = new Session(ByteBuffer.allocate(BUFSIZE),clntChan,true);
					senderSocketKey.attach(session);
					System.out.println("Acepto conexion.");*/
					SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
					SelectionKey clientKey = clntChan.register(selector, SelectionKey.OP_WRITE);
					clntChan.configureBlocking(false);					
					Session session = new Session(null,false);
					clientKey.attach(session);
					
				}
				if (key.isValid() && key.isReadable()) {
										
					Session session = (Session) key.attachment();
					SelectionKey ansKey = (SelectionKey) session.getKey();
					SocketChannel ansChannel= (SocketChannel) session.getChannel();
					
					if ( ansKey == null ) {
						
						Session session2 = new Session(key, (SocketChannel)key.channel(), false);
						ansKey = ansChannel.register(selector, SelectionKey.OP_WRITE, session2);
						session.setKey(ansKey);
					}
					SocketChannel channel = (SocketChannel) key.channel();
					ByteBuffer ansBuffer = ((Session)ansKey.attachment()).getBuffer();
					long bytesRead = channel.read(ansBuffer);
					
					if (bytesRead == -1) {
						ansKey.channel().close();
						channel.close();
					} else if (bytesRead > 0) {
						System.out.println(new String(ansBuffer.array()).substring(ansBuffer.arrayOffset(), ansBuffer.position()));
						ansKey.interestOps(SelectionKey.OP_WRITE);
					}
					System.out.println( bytesRead + " " + session.isFromServer());
				}
				if (key.isValid() && key.isWritable()) {
					Session session = (Session)key.attachment();
					ByteBuffer buf = session.getBuffer();
					buf.flip();
					SocketChannel clntChan = (SocketChannel) key.channel();
					clntChan.write(buf);
					if (!buf.hasRemaining()) {
						key.interestOps(SelectionKey.OP_READ);
					}
					buf.compact();
					
				}
				keyIter.remove();
			}
		}
		 
	}
}
