package pop3;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TCPPOP3ClientNonBlocking {
	public static void main(String[] args) throws IOException {
		//Test for correct # of args
		if((args.length<2)||(args.length>3)){ 
			throw new IllegalArgumentException("Parameter(s): <Server> <Word> [<Port>]");
		}
		// Server name or IP address
		String server = args[0]; 
		// Convert input String to bytes using the default charset
		byte[] argument = args[1].getBytes(); 
		
		int servPort = (args.length == 3) ? Integer.parseInt(args[2]) : 7;
		// Create channel and set to nonblocking
        SocketChannel clntChan = SocketChannel.open();
        clntChan.configureBlocking(false);
        
        // Initiate connection to server and repeatedly poll until complete
        if (!clntChan.connect(new InetSocketAddress(server, servPort))) {
            while (!clntChan.finishConnect()) {
            	// TODO: Do something else
                System.out.print(".");
            }
        }
        
        ByteBuffer writeBuf = ByteBuffer.wrap(argument);
        ByteBuffer readBuf = ByteBuffer.allocate(argument.length);
        // Total bytes received so far
        int totalBytesRcvd = 0;
        // Bytes received in last read
        int bytesRcvd = 0;
        
        while (totalBytesRcvd < argument.length) {
            if (writeBuf.hasRemaining()) {
                clntChan.write(writeBuf);
            }
            if ((bytesRcvd = clntChan.read(readBuf)) == -1) {
                throw new SocketException("Connection closed prematurely");
            }
            totalBytesRcvd += bytesRcvd;
            // TODO: Do something else
            System.out.print(".");
        }
        // TODO: do something with the info readed
        clntChan.close();
	}
}
