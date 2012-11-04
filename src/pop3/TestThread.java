package pop3;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class TestThread {

	public static void main(String[] args) throws IOException, InterruptedException {

		Selector s = Selector.open();
		ServerSocketChannel configListnChannel = ServerSocketChannel.open();
		ServerSocketChannel listnChannel = ServerSocketChannel.open();
		listnChannel.socket().bind(new InetSocketAddress("localhost", 5000));
		listnChannel.configureBlocking(false);
		SelectionKey k = listnChannel.register(s, SelectionKey.OP_ACCEPT);
		k.interestOps(0);
		Session session = new Session(k, null, false);
		k.attach(session);

		TransformThread t = new TransformThread("cat", k);
		System.out.println("asdd");
		t.start();
		ByteBuffer b = ByteBuffer.allocate(1024);
		b.put("Test".getBytes());
		b.flip();
		t.addBuffer(b);
		System.out.println("Agregue");
		t.finished();
		t.join();
		ByteBuffer ans = session.getWritingBuffer();
		System.out.println(new String(ans.array()).substring(ans.arrayOffset(),
				ans.position()));

	}
}
