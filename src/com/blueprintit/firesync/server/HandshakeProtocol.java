package com.blueprintit.firesync.server;

import com.blueprintit.firesync.TextProtocolHandler;
import java.io.IOException;

public class HandshakeProtocol extends TextProtocolHandler
{
	public void startProtocol()
	{
		sendLine("200 FireSyncServer ready for handshake");
	}
	
	public void lineReceived(String line)
	{
		if (line.toUpperCase().startsWith("QUIT"))
		{
			sendLine("200 Connection closing");
			closeConnection();
		}
		else if (line.toUpperCase().startsWith("STARTSSL"))
		{
			sendLine("500 SSL not yet implemented");
		}
		else
		{
			sendLine("400 Unknown request error");
		}
	}
	
	public void connectionClosed()
	{
	}
}
