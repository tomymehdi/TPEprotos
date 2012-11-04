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
	public static final String DEFAULT_USER_URL = "localhost";
	private static final int POP3_SERVER_PORT = 10000;

	private Selector selector;
	private Map<String, Transformation> transformations = new HashMap<String, Transformation>();
	private RstrictedIp restrictedIps;
	private Map<String, User> usersMap = new HashMap<String, User>();
	private ConfigurationProtocol configProtocol;
	private String transformerPath;

	// TODO revisar la clase AeSimpleMD5 para ver como se usa Message
	// diggest....

	public static void main(String[] args) throws IOException {
		ProxyServer proxy = new ProxyServer();
		User u = new User(DEFAULT_USER_URL);
		u.setMaxConnections(3);
		proxy.usersMap.put("jmozzino", u);
		proxy.transformerPath = "src/transformation/l337.jar";
//		proxy.transformerPath = "cat";
		proxy.run();
	}

	public void run() throws IOException {
		configProtocol = new ConfigurationProtocol(usersMap);
		this.selector = Selector.open();
		ServerSocketChannel listnChannel = ServerSocketChannel.open();
		ServerSocketChannel configListnChannel = ServerSocketChannel.open();

		listnChannel.socket().bind(new InetSocketAddress("localhost", 3000));
		listnChannel.configureBlocking(false);
		listnChannel.register(selector, SelectionKey.OP_ACCEPT);

		configListnChannel.socket().bind(
				new InetSocketAddress("localhost", 51914));
		configListnChannel.configureBlocking(false);
		configListnChannel.register(selector, SelectionKey.OP_ACCEPT);

		while (true) {
			if (selector.select(TIMEOUT) == 0) {
				System.out.print(".");
				continue;
			}
			Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
			while (keyIter.hasNext()) {
				SelectionKey key = keyIter.next();
				if (key.isValid() && key.isAcceptable()) {
					if (key.channel().equals(listnChannel)) {
						SocketChannel clntChan = ((ServerSocketChannel) key
								.channel()).accept();
						clntChan.configureBlocking(false);
						SelectionKey clientKey = clntChan.register(selector,
								SelectionKey.OP_WRITE);
						Session session = new Session(clientKey, clntChan,
								false);
						clientKey.attach(session);
						if (RestrictedIP.isBanned(clntChan.socket()
								.getInetAddress())) {
							session.setState(State.CONNECTION_ERROR);
							session.addToBuffer(("-ERR ROFL tu ip esta baneada\n")
									.getBytes());
						} else {
							session.addToBuffer(("+OK POP3 server Aloha\n")
									.getBytes());
						}
					} else if (key.channel().equals(configListnChannel)) {
						System.out.println("Acepto la otra");
						configProtocol.doAccept(selector, key);

					}
				}
				if (key.isValid() && key.isReadable()) {

					if (key.attachment() instanceof Session) {

						Session session = (Session) key.attachment();
						SelectionKey ansKey = (SelectionKey) session.getKey();
						SocketChannel ansChannel = (SocketChannel) session
								.getChannel();
						SocketChannel channel = (SocketChannel) key.channel();

						long bytesRead = 0;

						ByteBuffer auxBuffer = ByteBuffer
								.allocate(Session.BUFFER_SIZE);
						if (!session.isFromServer()) {
							bytesRead = channel.read(auxBuffer);
						} else {
							if (session.getState().equals(State.FIRST)
									|| session.getState().equals(
											State.CONNECTION_ERROR)) {
								ByteBuffer trash = ByteBuffer.allocate(1024);
								bytesRead = channel.read(trash);
							} else {
								bytesRead = channel.read(auxBuffer);
							}
						}

						if (bytesRead == -1) {
							ansKey.channel().close();
							channel.close();
						} else if (bytesRead > 0) {
							System.out.println(new String(auxBuffer.array())
									.substring(auxBuffer.arrayOffset(),
											auxBuffer.position()));
							if (!(session.isFromServer() && session.getState()
									.equals(State.FIRST))) {
								ansKey.interestOps(SelectionKey.OP_WRITE);
							} else {
								key.interestOps(SelectionKey.OP_WRITE);
							}
						}
						System.out.println(bytesRead + " "
								+ session.isFromServer());
						if (!session.isFromServer()) {
							Command command = session.processUser(auxBuffer);
							if (command != null) {
								processCommand(command, session, key);
							}
						} else {
							if (session.getState().equals(State.FIRST)) {
								session.setState(State.AUTH);
							} else if (session.getState().equals(
									State.FIRST_TRANSFORM)
									|| session.getState().equals(
											State.TRANSFORM)) {
								auxBuffer.flip();
								if (session.getState().equals(
										State.FIRST_TRANSFORM)) {
									//Sacar la primera linea +OK...
									ByteBuffer okBuffer = ByteBuffer.allocate(Session.BUFFER_SIZE);
									byte b;
									System.out.println(auxBuffer);
									while ( (b = auxBuffer.get()) != '\n') {
										okBuffer.put(b);
									}
									okBuffer.put(b);
									Session ansSession = ((Session) session
											.getKey().attachment());
									okBuffer.flip();
									//auxBuffer.compact();
									ansSession.addBuffer(okBuffer);
									session.runTransformation();
									session.setState(State.TRANSFORM);
								}
								boolean hasPoint = auxBuffer.get(auxBuffer.limit() - 2) == '.';
								session.addToTransfromThread(auxBuffer);
								if ( hasPoint ) {
									session.setState(State.TRANS);
									session.finishTransformation();
//									session.getKey().interestOps(SelectionKey.OP_WRITE);
								}
							} else if (!session.getState().equals(
									State.CONNECTION_ERROR)) {
								System.out.println("soy server\n");
								Session ansSession = ((Session) session
										.getKey().attachment());
								auxBuffer.flip();
								String ans = new String(auxBuffer.array())
								.substring(auxBuffer.arrayOffset(),
										auxBuffer.limit());
								ansSession.addBuffer(auxBuffer);
//								auxBuffer.compact();
								if (session.getState().equals(State.PASS)) {
									if (ans.toLowerCase().startsWith("+ok")) {
										User.ConnectionErrors error = session
												.getUser().connect();
										if (error != null) {
											ansSession
													.setState(State.CONNECTION_ERROR);
											ansSession.clearBuffers();
											ansSession.addToBuffer((error
													.getMessage() + "\n")
													.getBytes());
											session.addToBuffer("QUIT\n"
													.getBytes());
											session.setState(State.CONNECTION_ERROR);
											key.interestOps(SelectionKey.OP_WRITE);
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
					} else if (key.attachment() instanceof ByteBuffer) {
						System.out.println("Leo en la otra");
						configProtocol.doRead(selector, key);
					}
				}
				if (key.isValid() && key.isWritable()) {
					if (key.attachment() instanceof Session) {
						Session session = (Session) key.attachment();
						ByteBuffer buf = session.getWritingBuffer();
						if (buf == null) {
							key.interestOps(SelectionKey.OP_READ);
							keyIter.remove();
							continue;
						}
						SocketChannel clntChan = (SocketChannel) key.channel();
						System.out.println(new String(buf.array()).substring(
								buf.arrayOffset(), buf.limit()));
						clntChan.write(buf);
						System.out.println(System.currentTimeMillis());
						if (!buf.hasRemaining() && !session.canWrite()) {
							key.interestOps(SelectionKey.OP_READ);
						}
						if (session.getState().equals(State.CONNECTION_ERROR)) {
							SocketChannel channel = (SocketChannel) key
									.channel();
							channel.close();
						}
					} else if (key.attachment() instanceof ByteBuffer) {
						System.out.println("Escribo en la otra");
						configProtocol.doWrite(selector, key);
					}

				}
				keyIter.remove();
			}
		}

	}

	private void processCommand(Command command, Session session,
			SelectionKey key) throws IOException {
		if (command.getCommandName().equals("user")) {
			String[] split = command.getCommand().split(" ");
			if (split.length < 2) {
				Session ansSession = (Session) session.getKey().attachment();
				ansSession.addToBuffer(("-ERR Invalid arguments" + "\n")
						.getBytes());
				return;
			}
			String userName = command.getCommand().split(" ")[1];
			User user;
			String server = DEFAULT_USER_URL;
			if ((user = usersMap.get(userName)) != null
					&& user.getServer() != null) {
				// user configuration saved
				server = user.getServer();
			}

			SocketChannel senderSocket = SocketChannel
					.open(new InetSocketAddress(server, POP3_SERVER_PORT));
			senderSocket.configureBlocking(false);

			SelectionKey senderSocketKey = senderSocket.register(selector,
					SelectionKey.OP_READ);
			Session ansSession = new Session(key,
					(SocketChannel) key.channel(), true);
			session.setKey(senderSocketKey);
			session.setChannel(senderSocket);
			ansSession.addToBuffer((command.getCommand() + "\n").getBytes());
			ansSession.setState(State.FIRST);
			senderSocketKey.attach(ansSession);

			if (usersMap.containsKey(userName)) {
				ansSession.setUser(usersMap.get(userName));
				session.setUser(usersMap.get(userName));
			} else {
				User u = new User(server);
				ansSession.setUser(u);
				session.setUser(u);
			}
		} else {
			Session ansSession = (Session) session.getKey().attachment();
			ansSession.addToBuffer((command.getCommand() + "\n").getBytes());
			if (session.getState().equals(State.AUTH)
					&& command.getCommandName().equals("pass")) {
				ansSession.setState(State.PASS);
			} else if (session.getState().equals(State.TRANS)
					&& command.getCommandName().equals("retr") && transformerPath != null ) {
				ansSession.setState(State.FIRST_TRANSFORM);
				ansSession.transformUsing(transformerPath);
			}
		}
	}

}
