package com.blueprintit.firesync;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.security.GeneralSecurityException;
import java.nio.channels.SocketChannel;

public class ConnectionHandler
{
	private SocketChannel socket;
	private ByteBuffer sslbuffer;
	private DataHandler handler;
	SSLEngine ssl = null;
	
	public ConnectionHandler(SocketChannel channel)
	{
	}
	
	public void makeSecure() throws GeneralSecurityException
	{
		if (ssl==null)
		{
			ssl = SSLContext.getInstance("SSL").createSSLEngine();
			sslbuffer = ByteBuffer.allocateDirect(1024);
		}
	}
	
	public void setDataHandler(DataHandler handler)
	{
		this.handler = handler;
		handler.registerConnection(this);
	}
	
	public void sendData(ByteBuffer buffer) throws IOException
	{
		socket.write(buffer);
	}
	
	public void dataReceived(ByteBuffer buffer)
	{
		if (ssl!=null)
		{
			try
			{
				ssl.unwrap(buffer,sslbuffer);
			}
			catch (SSLException e)
			{
			}
			buffer=sslbuffer;
		}
		if (handler!=null)
		{
			handler.dataReceived(buffer);
		}
		if (sslbuffer!=null)
		{
			sslbuffer.clear();
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
