package com.blueprintit.firesync;

import java.nio.ByteBuffer;
import java.io.InputStream;
import java.io.PipedOutputStream;
import java.io.PipedInputStream;

public abstract class ThreadedDataHandler extends AbstractDataHandler implements Runnable
{
	private ConnectionHandler connection;
	private PipedOutputStream pipe;
	
	public void sendData(ByteBuffer buffer)
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
	
	public void startProtocol()
	{
		pipe = new PipedOutputStream();
		(new Thread(this)).start();
	}
	
	public void dataReceived(ByteBuffer buffer)
	{
		try
		{
			if (buffer.hasArray())
			{
				byte[] data = buffer.array();
				int ofs = buffer.arrayOffset();
				pipe.write(data,ofs,buffer.remaining());
				buffer.position(buffer.limit());
			}
			else
			{
				byte[] data = new byte[buffer.remaining()];			
				buffer.get(data);
				pipe.write(data);
			}
		}
		catch (Exception e)
		{
			System.err.println("Error passsing data to thread - "+e.getMessage());
			e.printStackTrace();
			connectionClosed();
		}
	}
	
	public abstract void protocolThread(InputStream input);
	
	public abstract void connectionClosed();
	
	public void run()
	{
		try
		{
			PipedInputStream input = new PipedInputStream(pipe);
			protocolThread(input);
		}
		catch (Exception e)
		{
			System.err.println("Error, unable to connect pipes - "+e.getMessage());
			e.printStackTrace();
			connectionClosed();
		}
	}
}
