package com.blueprintit.firesync;

import java.nio.ByteBuffer;
import java.io.IOException;

public abstract class TextProtocolHandler extends AbstractDataHandler
{
	private ByteBuffer sendbuffer = ByteBuffer.allocateDirect(1024);
	private StringBuilder readbuffer = new StringBuilder();
	private byte[] inputbuffer = new byte[1024];
	
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
		int stpos = buffer.position();
		StringBuilder builder = new StringBuilder();
		while (buffer.hasRemaining())
		{
			String bit = new String(new byte[] {buffer.get()});
			if (bit.charAt(0)=='\n')
			{
				lineReceived(builder.toString());
				builder.delete(0,builder.length());
				stpos = buffer.position();
			}
			else
			{
				builder.append(bit.charAt(0));
			}
		}
		buffer.position(stpos);
	}
}
