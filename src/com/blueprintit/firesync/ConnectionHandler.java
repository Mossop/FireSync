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
	
	public ConnectionHandler(SocketChannel channel, DataHandler handler)
	{
		databuffer = ByteBuffer.allocate(1024);
		socket=channel;
		setDataHandler(handler);
	}
	
	public void makeSecure() throws GeneralSecurityException
	{
		if (ssl==null)
		{
			ssl = SSLContext.getInstance("SSL").createSSLEngine();
			sslbuffer = ByteBuffer.allocateDirect(ssl.getSession().getPacketBufferSize());
			databuffer = ByteBuffer.allocateDirect(ssl.getSession().getApplicationBufferSize());
		}
	}
	
	private void setDataHandler(DataHandler handler)
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
		handler.dataReceived(databuffer);
		databuffer.clear();
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
						sslbuffer=newssl;
					}
					else
					{
						sslbuffer.clear();
					}
					if (result.getHandshakeStatus()==NEED_WRAP)
					{
						ByteBuffer blank = ByteBuffer.allocate(1);
						blank.limit(0);
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
		try
		{
			socket.close();
		}
		catch (Exception e) {}
	}
}
