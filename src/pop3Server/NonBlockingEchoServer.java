package pop3Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NonBlockingEchoServer {

	public static void main(String args[]) throws IOException {
		Selector selector = Selector.open();
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().bind(
				new InetSocketAddress("localhost", 10000));
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		while (!Thread.interrupted()) {

			if (selector.select(1000) == 0) {
				continue;
			}

			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();
				if (key.isAcceptable()) {
					System.out.println("Aceptando");
					SocketChannel client = ((ServerSocketChannel) key.channel())
							.accept();
					client.configureBlocking(false);
					SelectionKey k = client.register(selector,
							SelectionKey.OP_WRITE, ByteBuffer.allocate(20000));
					((ByteBuffer) k.attachment()).put("+OK\n".getBytes());

				}
				if (key.isReadable()) {
					System.out.println("Leyendo");
					SocketChannel client = (SocketChannel) key.channel();
					ByteBuffer aux = ByteBuffer.allocate(1024);
					int readBytes = client.read(aux);
					if (new String(aux.array()).startsWith("retr ")) {
						String mail = "";
						// for ( int i = 0 ; i < 5098 ; i++ ) {
						// mail+="qwe";
						// if ( i % 100 == 0 && i != 0 ) {
						// mail+="\n";
						// }
						// }
						String line = "";
						BufferedReader br = new BufferedReader(new FileReader(
								"mimeExamples/mimeExample.txt"));
						while ((line = br.readLine()) != null) {
							mail += line + '\n';
						}

						((ByteBuffer) key.attachment())
								.put(("+OK Here goes the mail!\n" + mail + "\n.\n")
										.getBytes());
					} else {
						((ByteBuffer) key.attachment()).put("+OK\n".getBytes());
					}
					// int readBytes = client.read((ByteBuffer)
					// key.attachment());
					if (readBytes == -1) {
						client.close();
					} else if (readBytes > 0) {
						key.interestOps(SelectionKey.OP_READ
								| SelectionKey.OP_WRITE);
					}
				}
				if (key.isValid() && key.isWritable()) {
					System.out.println("Escribiendo");
					SocketChannel client = (SocketChannel) key.channel();
					ByteBuffer buffer = (ByteBuffer) key.attachment();
					buffer.flip();
					client.write(buffer);
					if (!buffer.hasRemaining()) {
						key.interestOps(SelectionKey.OP_READ);
					}
					buffer.compact();
				}
				it.remove();
			}
		}
	}
}