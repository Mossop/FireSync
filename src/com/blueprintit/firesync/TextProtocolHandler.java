package com.blueprintit.firesync;

import java.nio.ByteBuffer;
import java.io.IOException;

public abstract class TextProtocolHandler extends AbstractDataHandler
{
	private ByteBuffer sendbuffer = ByteBuffer.allocateDirect(1024);
	private StringBuilder readbuffer = new StringBuilder();
	
	public void sendLine(String text)
	{
		if (!text.endsWith("\n"))
		{
			text=text+"\n";
		}
		byte[] data = text.getBytes();
		int start = 0;
		while (start<data.length)
		{
			int len = Math.min(sendbuffer.remaining(),data.length-start);
			sendbuffer.put(data,start,len);
			start+=len;
			sendbuffer.flip();
			sendData(sendbuffer);
			sendbuffer.clear();
		}
	}
	
	public abstract void lineReceived(String text);
	
	public void dataReceived(ByteBuffer buffer)
	{
		while (buffer.hasRemaining())
		{
			String bit = new String(new byte[] {buffer.get()});
			if (bit.charAt(0)=='\r')
			{
			}
			else if (bit.charAt(0)=='\n')
			{
				lineReceived(readbuffer.toString());
				readbuffer.delete(0,readbuffer.length());
			}
			else
			{
				readbuffer.append(bit);
			}
		}
	}
}
