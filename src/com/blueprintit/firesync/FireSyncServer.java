package com.blueprintit.firesync;

import static java.nio.channels.SelectionKey.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Map;
import java.net.InetSocketAddress;
import java.util.Iterator;

public class FireSyncServer implements Runnable
{
	private boolean shutdown;
	private Map<SocketChannel,ConnectionHandler> handlers;
	
	public FireSyncServer()
	{
		handlers = new HashMap<SocketChannel,ConnectionHandler>();
	}
	
	public void init(String[] args)
	{
	}
	
	public void start()
	{
		shutdown=false;
		(new Thread(this)).start();
	}
	
	public void run()
	{
		ByteBuffer buffer;
		ServerSocketChannel listener;
		Selector selector;
		try
		{
			listener = ServerSocketChannel.open();
			listener.configureBlocking(false);
			listener.socket().bind(new InetSocketAddress(6666));
			selector = Selector.open();
			listener.register(selector,OP_ACCEPT);
			System.out.println("Listening on port 6666");
			buffer = ByteBuffer.allocateDirect(1024);
		}
		catch (Exception e)
		{
			System.err.println("Unable to start listener - "+e.getMessage());
			e.printStackTrace();
			return;
		}
		while (!shutdown)
		{
			Iterator i = selector.selectedKeys().iterator();
			while (i.hasNext())
			{
				SelectionKey sel = (SelectionKey)i.next();
				if (sel.isAcceptable())
				{
					assert sel.channel()==listener;
					try
					{
						SocketChannel socket = listener.accept();
						System.out.println("Connection accepted");
						try
						{
							socket.register(selector,OP_READ);
							ConnectionHandler handler = new ConnectionHandler(socket);
							handlers.put(socket,handler);
						}
						catch (Exception e)
						{
							System.err.println("Error registering socket - "+e.getMessage());
						}
					}
					catch (Exception e)
					{
						System.err.println("Error accepting connection - "+e.getMessage());
					}
					selector.selectedKeys().remove(sel);
				}
				else if (sel.isReadable())
				{
					assert sel.channel() instanceof SocketChannel;
					int count;
					try
					{
						count = ((SocketChannel)sel.channel()).read(buffer);
					}
					catch (Exception e)
					{
						count=-1;
					}
					if (count>0)
					{
						buffer.flip();
						handlers.get((SocketChannel)sel.channel()).dataReceived(buffer);
						buffer.clear();
					}
					else
					{
						handlers.get((SocketChannel)sel.channel()).connectionClosed();
					}
					selector.selectedKeys().remove(sel);
				}
			}
		}
	}
	
	public void stop()
	{
		shutdown=true;
	}
	
	public void destroy()
	{
	}
	
	public static void main(String[] args)
	{
		FireSyncServer server = new FireSyncServer();
		server.init(args);
		server.start();
	}
}
