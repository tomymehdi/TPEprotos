package pop3;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import pop3.restriction.FromRestriction;
import pop3.restriction.Restriction;
import pop3.restriction.SizeRestriction;

public class ConfigurationProtocol {

	ProxyServer proxy;

	public ConfigurationProtocol(ProxyServer proxy) {
		this.proxy = proxy;
	}

	public void doAccept(Selector selector, SelectionKey key)
			throws IOException {
		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
		clntChan.configureBlocking(false);
		clntChan.register(selector, SelectionKey.OP_READ,
				ByteBuffer.allocate(Session.BUFFER_SIZE));
	}

	public void doRead(Selector selector, SelectionKey key) throws IOException {
		ByteBuffer buf = (ByteBuffer) key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();
		int bytesRead = channel.read(buf);
		if (bytesRead == -1) {
			channel.close();
		} else if (bytesRead > 0) {
			buf.flip();
			byte[] byteArray = new byte[buf.remaining()];
			buf.get(byteArray);
			String command = new String(byteArray);
			System.out.println(command);
			buf.clear();
			buf.put(executeCommand(parseCommand(command), key).getBytes());
			key.interestOps(SelectionKey.OP_WRITE);
		}
	}

	public void doWrite(Selector selector, SelectionKey key) throws IOException {
		ByteBuffer buf = (ByteBuffer) key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();
		buf.flip();
		channel.write(buf);
		if (!buf.hasRemaining()) {
			key.interestOps(SelectionKey.OP_READ);
		}
		buf.compact();
	}

	private String[] parseCommand(String command) {
		command = command.substring(0, command.indexOf('\n')).replaceAll(
				"[\t ]+", " ");
		return command.split(" ");
	}

	private String executeCommand(String[] args, SelectionKey key) {
		if (args.length == 0) {
			return "-ERR Unknown command\n";
		}
		String command = args[0].toUpperCase();
		System.out.println(command);
		args = Arrays.copyOfRange(args, 1, args.length);
		if (command.equals("AUTH")) {
			return executeAuth(args);
		}
		if (command.equals("SERVER")) {
			return executeServer(args);
		}
		if (command.equals("DSERVER")) {
			return executeDefaultServer(args);
		}
		if (command.equals("RESTRICT")) {
			return executeRestrict(args);
		}
		if (command.equals("RESTRICTIP")) {
			return executeRestrictIp(args);
		}
		if (command.equals("STAT")) {
			return executeStat(args);
		}
		if (command.equals("CLOSE")) {
			return executeClose(args, key);
		}
		return "-ERR Unknown command\n";
	}

	private String executeClose(String[] args, SelectionKey key) {
		try {
			key.channel().close();
		} catch (IOException e) {
			return "-ERR Couldn't close the channel\n";
		}
		return "+OK See ya!\n";
	}

	private String executeStat(String[] args) {
		// TODO Recolectar estadisticas
		return "+OK\n";
	}

	private String executeRestrictIp(String[] args) {
		if (args.length < 1) {
			return "-ERR Invalid arguments\n";
		}
		try {
			Inet4Address ip = (Inet4Address) Inet4Address.getByName(args[0]);
			System.out.println(ip.toString());
			blockIp(ip);
			return "+OK " + args[0] + " blocked\n";
		} catch (UnknownHostException e) {
			return "-ERR Host not found\n";
		}
	}

	private void blockIp(Inet4Address ip) {
		// TODO Poner el codigo que va
	}

	private String executeRestrict(String[] args) {
		if (args.length < 3) {
			return "-ERR Invalid arguments\n";
		}
		String username = args[0], type = args[1].toUpperCase();
		args = Arrays.copyOfRange(args, 2, args.length);
		if (type.equals("TIME")) {
			if (args.length < 2) {
				return "-ERR Invalid arguments\n";
			}
			int from = Integer.valueOf(args[0]), to = Integer.valueOf(args[1]);
			return executeTimeRestrict(username, from, to);
		}
		if (type.equals("LOGIN")) {
			if (args.length < 1) {
				return "-ERR Invalid arguments\n";
			}
			int timesPerDay = Integer.valueOf(args[0]);
			return executeLoginRestrict(username, timesPerDay);
		}
		if (type.equals("DELETE")) {
			if (args.length < 2) {
				return "-ERR Invalid arguments\n";
			}
			String delType = args[0].toUpperCase();
			return executeDeleteRestrict(username, delType,
					Arrays.copyOfRange(args, 1, args.length));
		}
		return "-ERR Unknown type\n";
	}

	private String executeDeleteRestrict(String username, String delType,
			String[] condition) {
		if (condition.length < 1) {
			return "-ERR Invalid arguments";
		}
		if (delType.equals("DATE")) {
			return setStructureRestriction(username, condition);
		}
		if (delType.equals("FROM")) {
			return setFromRestriction(username, condition);
		}
		if (delType.equals("HEADER")) {
			return setHeaderRestriction(username, condition);
		}
		if (delType.equals("CTYPE")) {
			return setContentTypeRestriction(username, condition);
		}
		if (delType.equals("SIZE")) {
			return setSizeRestriction(username, condition);
		}
		if (delType.equals("STRUCT")) {
			return setStructureRestriction(username, condition);
		}
		return "-ERR Invalid type\n";
	}

	private String setFromRestriction(String username, String[] condition) {

		if (condition.length < 2) {
			return "-ERR Invalid arguments\n";
		}
		boolean exactMatch = Boolean.valueOf(condition[1]);
		return addRestriction(username, new FromRestriction(condition[0],
				exactMatch));

	}

	private String addRestriction(String username, Restriction restriction) {
		User u = proxy.getUsersMap().get(username);
		if (u == null) {
			u = new User(proxy.getDefaultServer());
		}
		u.addRestriction(restriction);
		return "+OK Restriction added\n";
	}

	private String setHeaderRestriction(String username, String[] condition) {
		// TODO Auto-generated method stub
		return null;
	}

	private String setContentTypeRestriction(String username, String[] condition) {
		// TODO Auto-generated method stub
		return null;
	}

	private String setSizeRestriction(String username, String[] condition) {
		if (condition.length < 1) {
			return "-ERR Invalid arguments\n";
		}
		try {
			return addRestriction(username,
					new SizeRestriction(Integer.valueOf(condition[0])));
		} catch (NumberFormatException e) {
			return "-ERR Invalid arguments\n";
		}
	}

	private String setStructureRestriction(String username, String[] condition) {
		// TODO Auto-generated method stub
		return null;
	}

	private String executeLoginRestrict(String username, int timesPerDay) {
		User u;
		if (proxy.getUsersMap().containsKey(username)) {
			u = proxy.getUsersMap().get(username);
		} else {
			u = new User(proxy.getDefaultServer());
		}
		proxy.getUsersMap().put(username, u);
		u.setMaxConnections(timesPerDay);
		return "+OK!\n";
	}

	private String executeTimeRestrict(String username, int from, int to) {
		User u;
		if (proxy.getUsersMap().containsKey(username)) {
			u = proxy.getUsersMap().get(username);
		} else {
			u = new User(proxy.getDefaultServer());
		}
		proxy.getUsersMap().put(username, u);
		u.addAllowedConnectionInterval(from, to);
		return "+OK!\n";
	}

	private String executeDefaultServer(String[] args) {
		if (args.length < 1) {
			return "-ERR Invalid arguments\n";
		}
		setDefaultServer(args[0]);
		return "+OK Default server changed to " + args[0];
	}

	private void setDefaultServer(String string) {
		proxy.setDefaultServer(string);
	}

	private String executeServer(String[] args) {
		if (args.length < 1) {
			return "-ERR Invalid arguments\n";
		}
		String username = args[0];
		if (username.equals("*")) {
			for (String user : proxy.getUsersMap().keySet()) {
				setServer(user, args.length == 1 ? null : args[1]);
			}
			if (args.length != 1) {
				setDefaultServer(args[1]);
			}
		} else {
			setServer(username, args.length == 1 ? null : args[1]);
		}
		return "+OK " + username + "'s server changed to " + args[1];
	}

	private void setServer(String username, String server) {
		User u = proxy.getUsersMap().get(username);
		if (u == null) {
			proxy.getUsersMap().put(username, new User(server));
		} else {
			u.setServer(server);
		}
	}

	private String executeAuth(String[] args) {
		if (args.length < 2) {
			return "-ERR Invalid arguments\n";
		}
		String username = args[0], password = args[1];
		System.out.println(username + "@" + password);
		if (authenticate(username, password)) {
			return "+OK Welcome professor\n";
		}
		return "-ERR Incorrect username or password\n";
	}

	private boolean authenticate(String username, String password) {
		if (username.equals("xp500") && password.equals("asd")) {
			return true;
		}
		return false;
	}
}
