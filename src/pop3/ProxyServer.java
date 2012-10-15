package pop3;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ProxyServer {

	private static final int BUFSIZE = 256; // Buffer size (bytes)
	private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)


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
				if (key.isAcceptable()) {
					SocketChannel senderSocket = SocketChannel
							.open(new InetSocketAddress("pop3.alu.itba.edu.ar",
									110));
					senderSocket.configureBlocking(false);
					SelectionKey senderSocketKey = senderSocket.register(
							selector, SelectionKey.OP_READ);
					SocketChannel clntChan = ((ServerSocketChannel) key
							.channel()).accept();
					clntChan.configureBlocking(false);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("buffer", ByteBuffer.allocate(1024));
					map.put("key", null);
					map.put("channel", clntChan);
					senderSocketKey.attach(map);
					System.out.println("Acepto conexion.");
				}
				if (key.isReadable()) {
					Map<String, Object> map = (Map<String, Object>) key
							.attachment();
					SelectionKey ansKey = (SelectionKey) map.get("key");
					SocketChannel ansChannel= (SocketChannel) map.get("channel");
					
					if ( ansKey == null ) {
						Map<String, Object> map1 = new HashMap<String, Object>();
						map1.put("buffer", ByteBuffer.allocate(1024));
						map1.put("key", key);
						map1.put("channel", key.channel());
						ansKey = ansChannel.register(selector, SelectionKey.OP_WRITE, map1);
						map.put("key", ansKey);
					}
					SocketChannel channel = (SocketChannel) key.channel();
					ByteBuffer ansBuffer = (ByteBuffer) ((Map)ansKey.attachment()).get("buffer");
					long bytesRead = channel.read(ansBuffer);
					System.out.println("LEO "+bytesRead);
					if (bytesRead == -1) {
						ansKey.channel().close();
						channel.close();
					} else if (bytesRead > 0) {
						System.out.println(new String(ansBuffer.array()).substring(ansBuffer.arrayOffset(), ansBuffer.position()));
						ansKey.interestOps(SelectionKey.OP_WRITE);
					}
				}
				if (key.isValid() && key.isWritable()) {
					Map<String, Object> map = (Map<String, Object>) key
							.attachment();
					ByteBuffer buf = (ByteBuffer) map.get("buffer");
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
