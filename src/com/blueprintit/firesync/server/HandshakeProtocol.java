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
		if (line.equals("QUIT"))
		{
			System.out.println("Client quit");
			sendLine("200 Connection closing");
			closeConnection();
		}
		else if (line.equals("STARTSSL"))
		{
			sendLine("500 SSL not yet implemented");
		}
		else if (line.startsWith("PROTOCOL "))
		{
			String protocol = line.substring(9);
			System.out.println("Client request protocol "+protocol);
			sendLine("500 Protocol unsupported");
		}
		else
		{
			System.out.println("Client sent unknown command - \""+line+"\"");
			sendLine("400 Unknown request error");
		}
	}
	
	public void connectionClosed()
	{
	}
}
