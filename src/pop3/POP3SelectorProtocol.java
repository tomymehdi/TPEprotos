package pop3;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class POP3SelectorProtocol implements TCPProtocol{
	private int bufSize; // Size of I/O buffer

    public POP3SelectorProtocol(int bufSize) {
        this.bufSize = bufSize;
    }

	@Override
	public void handleAccept(SelectionKey key) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleRead(SelectionKey key) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleWrite(SelectionKey key) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
