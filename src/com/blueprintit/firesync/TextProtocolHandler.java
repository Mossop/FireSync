package com.blueprintit.firesync;

import java.nio.ByteBuffer;
import java.io.IOException;

public abstract class TextProtocolHandler extends AbstractDataHandler
{
	private ByteBuffer sendbuffer = ByteBuffer.allocateDirect(1024);
	private StringBuilder readbuffer = new StringBuilder();
	private byte[] inputbuffer = new byte[1024];
	
	public void sendLine(String text) throws IOException
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
		while (buffer.remaining()>0)
		{
			int len = Math.min(buffer.remaining(),inputbuffer.length);
			buffer.get(inputbuffer,0,Math.min(inputbuffer.length,buffer.remaining()));
			readbuffer.append(new String(inputbuffer,0,len));
		}
		int pos = readbuffer.indexOf("\n");
		while (pos>=0)
		{
			String line = readbuffer.substring(0,pos);
			lineReceived(line);
			readbuffer.delete(0,pos+1);
			pos=readbuffer.indexOf("\n");
		}
	}
}
