package com.blueprintit.firesync;

import java.nio.ByteBuffer;

public interface DataHandler
{
	public void registerConnection(ConnectionHandler connection);
	
	public void dataReceived(ByteBuffer data);
	
	public void connectionClosed();
}
