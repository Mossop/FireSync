package com.blueprintit.firesync;

import java.nio.ByteBuffer;
import java.io.IOException;

public abstract class AbstractDataHandler implements DataHandler
{
	private ConnectionHandler connection;
	
	public void sendData(ByteBuffer buffer) throws IOException
	{
		connection.sendData(buffer);
	}
	
	public void registerConnection(ConnectionHandler handler)
	{
		connection=handler;
		startProtocol();
	}
	
	public void closeConnection()
	{
		connection.closeConnection();
	}
	
	public abstract void startProtocol();
	
	public abstract void dataReceived(ByteBuffer buffer);
	
	public abstract void connectionClosed();
}
