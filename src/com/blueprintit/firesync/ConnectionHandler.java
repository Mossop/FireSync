package com.blueprintit.firesync;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.security.GeneralSecurityException;
import java.nio.channels.SocketChannel;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.*;

public class ConnectionHandler
{
	private SocketChannel socket;
	private ByteBuffer sslbuffer;
	private ByteBuffer databuffer;
	private DataHandler handler;
	SSLEngine ssl = null;
	
	public ConnectionHandler(SocketChannel channel)
	{
		databuffer = ByteBuffer.allocate(1024);
		socket=channel;
	}
	
	public void makeSecure() throws GeneralSecurityException
	{
		if (ssl==null)
		{
			ssl = SSLContext.getInstance("SSL").createSSLEngine();
			sslbuffer = ByteBuffer.allocateDirect(ssl.getSession().getPacketBufferSize());
		}
	}
	
	public void setDataHandler(DataHandler handler)
	{
		this.handler = handler;
		handler.registerConnection(this);
	}
	
	public void sendData(ByteBuffer buffer)
	{
		try
		{
			if (ssl!=null)
			{
				ByteBuffer sendbuffer = ByteBuffer.allocate(ssl.getSession().getPacketBufferSize());
				ssl.wrap(buffer,sendbuffer);
				sendbuffer.flip();
				socket.write(sendbuffer);
			}
			else
			{
				socket.write(buffer);
			}
		}
		catch (Exception e)
		{
			System.err.println("Error sending data - "+e.getMessage());
			e.printStackTrace();
			connectionClosed();
		}
	}
	
	public void closeConnection()
	{
		try
		{
			System.out.println("Closing connection");
			socket.close();
		}
		catch (Exception e)
		{
		}
	}
	
	private void sendData()
	{
		if (handler!=null)
		{
			handler.dataReceived(databuffer);
			if (databuffer.hasRemaining())
			{
				ByteBuffer newbuffer = ByteBuffer.allocate(1024);
				newbuffer.put(databuffer);
				databuffer = newbuffer;
			}
			else
			{
				databuffer.clear();
			}
		}
	}
	
	public boolean attemptRead(ByteBuffer buffer)
	{
		int count;
		try
		{
			count = socket.read(buffer);
		}
		catch (Exception e)
		{
			count=-1;
		}
		if (count>0)
		{
			return true;
		}
		else
		{
			System.err.println("Client disconnected");
			connectionClosed();
			return false;
		}
	}
	
	public void dataReceived()
	{
		if (ssl!=null)
		{
			if (attemptRead(sslbuffer))
			{
				sslbuffer.flip();
				try
				{
					SSLEngineResult result = ssl.unwrap(sslbuffer,databuffer);
					if (result.bytesProduced()>0)
					{
						databuffer.flip();
						sendData();
					}
					if (sslbuffer.hasRemaining())
					{
						ByteBuffer newssl = ByteBuffer.allocate(ssl.getSession().getPacketBufferSize());
						newssl.put(sslbuffer);
					}
					else
					{
						sslbuffer.clear();
					}
					if (result.getHandshakeStatus()==NEED_WRAP)
					{
						ByteBuffer blank = ByteBuffer.allocate(1);
						blank.flip();
						sendData(blank);
					}
				}
				catch (SSLException e)
				{
					System.err.println("Error processing SSL data - "+e.getMessage());
					e.printStackTrace();
					connectionClosed();
				}
			}
		}
		else
		{
			if (attemptRead(databuffer))
			{
				databuffer.flip();
				sendData();
			}
		}
	}
	
	public void connectionClosed()
	{
		if (handler!=null)
		{
			handler.connectionClosed();
		}
	}
}
