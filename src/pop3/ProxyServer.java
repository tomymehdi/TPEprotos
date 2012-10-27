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
import java.util.Map;

import pop3.Session.Command;
import pop3.Session.State;

public class ProxyServer {

	private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)
	private static final String DEFAULT_USER_URL = "pop3.alu.itba.edu.ar";

	private Selector selector;
	private Map<String, Transformation> transformations = new HashMap<String, Transformation>();
	private RstrictedIp restrictedIps;
	private Map<String, User> usersMap = new HashMap<String, User>();

	// TODO revisar la clase AeSimpleMD5 para ver como se usa Message
	// diggest....

	public static void main(String[] args) throws IOException {
		ProxyServer proxy = new ProxyServer();
		User u = new User(DEFAULT_USER_URL);
		u.setMaxConnections(3);
		proxy.usersMap.put("tmehdi", u);
		proxy.run();
	}

	public void run() throws IOException {
		this.selector = Selector.open();
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
					/*
					 * SocketChannel senderSocket = SocketChannel .open(new
					 * InetSocketAddress("pop3.alu.itba.edu.ar", 110));
					 * senderSocket.configureBlocking(false); SelectionKey
					 * senderSocketKey = senderSocket.register( selector,
					 * SelectionKey.OP_READ); SocketChannel clntChan =
					 * ((ServerSocketChannel) key .channel()).accept();
					 * clntChan.configureBlocking(false);
					 * 
					 * Session session = new
					 * Session(ByteBuffer.allocate(BUFSIZE),clntChan,true);
					 * senderSocketKey.attach(session);
					 * System.out.println("Acepto conexion.");
					 */
					SocketChannel clntChan = ((ServerSocketChannel) key
							.channel()).accept();
					clntChan.configureBlocking(false);
					SelectionKey clientKey = clntChan.register(selector,
							SelectionKey.OP_WRITE);
					Session session = new Session(clientKey, clntChan, false);
					clientKey.attach(session);
					if (RestrictedIP.isBanned(clntChan.socket()
							.getInetAddress())) {
						session.setState(State.CONNECTION_ERROR);
						session.getBuffer().put(
								new String("-ERR ROFL tu ip esta baneada\n")
										.getBytes());
					} else {
						session.getBuffer().put(
								new String("+OK POP3 server Aloha <"
										+ session.getDigest() + ">\n")
										.getBytes());
					}

				}
				if (key.isValid() && key.isReadable()) {

					Session session = (Session) key.attachment();
					SelectionKey ansKey = (SelectionKey) session.getKey();
					SocketChannel ansChannel = (SocketChannel) session
							.getChannel();

					// if ( ansKey == null ) {
					// ansChannel = session
					// Session session2 = new Session(key,
					// (SocketChannel)key.channel(), false);
					// ansChannel.configureBlocking(false);
					// ansKey = ansChannel.register(selector,
					// SelectionKey.OP_WRITE, session2);
					// session.setKey(ansKey);
					// }

					SocketChannel channel = (SocketChannel) key.channel();
					ByteBuffer buffer = session.getBuffer();

					long bytesRead = 0;

					if (!session.isFromServer()) {
						bytesRead = channel.read(buffer);
					} else {
						if (session.getState().equals(State.FIRST)) {

							ByteBuffer trash = ByteBuffer.allocate(1024);
							bytesRead = channel.read(trash);
							String aux = new String(trash.array()).substring(
									trash.arrayOffset(), trash.position());
							int minor = aux.indexOf('<');
							int mayor = aux.indexOf('>');
							if (minor != -1 && mayor != -1
									&& minor < aux.length() - 1) {
								session.setDigest(aux.substring(minor + 1,
										mayor));
							}
							System.out.println(session.getDigest());
							System.out.println(new String(trash.array())
									.substring(trash.arrayOffset(),
											trash.position()));
						} else {
							bytesRead = channel.read(buffer);
						}
					}

					if (bytesRead == -1) {
						ansKey.channel().close();
						channel.close();
					} else if (bytesRead > 0) {
						System.out.println(new String(buffer.array())
								.substring(buffer.arrayOffset(),
										buffer.position()));
						if (!(session.isFromServer() && session.getState()
								.equals(State.FIRST))) {
							ansKey.interestOps(SelectionKey.OP_WRITE);
						} else {
							key.interestOps(SelectionKey.OP_WRITE);
						}
					}
					System.out
							.println(bytesRead + " " + session.isFromServer());
					if (!session.isFromServer()) {

						Command command = session.processUser(session
								.getBuffer());
						processCommand(command, session, key);
					} else {
						if (session.getState().equals(State.FIRST)) {
							session.setState(State.AUTH);
						} else {
							System.out.println("soy server\n");
							Session ansSession = ((Session) session.getKey()
									.attachment());
							String ans = new String(buffer.array()).substring(
									buffer.arrayOffset(), buffer.position());
							buffer.flip();
							ansSession.getBuffer().put(buffer);
							buffer.compact();
							if (session.getState().equals(State.PASS)) {
								if (ans.toLowerCase().startsWith("+ok")) {
									if (!session.getUser().connect()) {
										ansSession.setState(State.CONNECTION_ERROR);
										ansSession.getBuffer().clear();
										ansSession.getBuffer().put("-ERR Excedio la cantidad maxima de conexiones\n".getBytes());
									} else {
										session.setState(State.TRANS);
										ansSession.setState(State.TRANS);
									}
								} else {
									session.setState(State.AUTH);
								}
							}
						}
					}
				}
				if (key.isValid() && key.isWritable()) {
					Session session = (Session) key.attachment();
					ByteBuffer buf = session.getBuffer();
					buf.flip();
					SocketChannel clntChan = (SocketChannel) key.channel();
					System.out.println(new String(buf.array()).substring(
							buf.arrayOffset(), buf.limit()));
					clntChan.write(buf);
					if (!buf.hasRemaining()) {
						key.interestOps(SelectionKey.OP_READ);
					}
					buf.compact();
					if (session.getState().equals(State.CONNECTION_ERROR)) {
						SocketChannel channel = (SocketChannel) key.channel();
						channel.close();
						if ( session.getChannel().equals(channel) ) {
							session.getChannel().close();
						}
					}

				}
				keyIter.remove();
			}
		}

	}

	private void processCommand(Command command, Session session,
			SelectionKey key) throws IOException {
		if (command == null) {
			session.getBuffer().put("-ERR\n".getBytes());
			return;
		}
		if (command.getCommandName().equals("user")) {
			String userName = command.getCommand().split(" ")[1];
			User user;
			String server = DEFAULT_USER_URL;
			if ((user = usersMap.get(userName)) != null
					&& user.getServer() != null) {
				// user configuration saved
				server = user.getServer();
			}

			SocketChannel senderSocket = SocketChannel
					.open(new InetSocketAddress(server, 110));
			senderSocket.configureBlocking(false);

			SelectionKey senderSocketKey = senderSocket.register(selector,
					SelectionKey.OP_READ);
			Session ansSession = new Session(key,
					(SocketChannel) key.channel(), true);
			session.setKey(senderSocketKey);
			session.setChannel(senderSocket);
			ansSession.getBuffer()
					.put((command.getCommand() + "\n").getBytes());
			ansSession.setState(State.FIRST);
			senderSocketKey.attach(ansSession);

			
			if ( usersMap.containsKey(userName) ) {
				ansSession.setUser(usersMap.get(userName));
				session.setUser(usersMap.get(userName));
			} else {
				User u = new User(server);
				ansSession.setUser(u);
				session.setUser(u);
			}
			
			// session.getBuffer().put("+OK\n".getBytes());
			// session.setChannel(senderSocket);

		} else {
			Session ansSession = (Session) session.getKey().attachment();
			ansSession.getBuffer()
					.put((command.getCommand() + "\n").getBytes());
			if (session.getState().equals(State.AUTH)
					&& command.getCommandName().equals("pass")) {
				ansSession.setState(State.PASS);
			}
		}
	}

}
