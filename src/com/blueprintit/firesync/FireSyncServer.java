package com.blueprintit.firesync;

import static java.nio.channels.SelectionKey.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Map;
import java.net.InetSocketAddress;
import java.util.Iterator;
import com.blueprintit.firesync.server.HandshakeProtocol;

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
			try
			{
				selector.select();
			}
			catch (Exception e)
			{
				System.err.println("Error selecting events - "+e.getMessage());
				e.printStackTrace();
			}
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
							socket.configureBlocking(false);
							socket.register(selector,OP_READ);
							ConnectionHandler handler = new ConnectionHandler(socket);
							handlers.put(socket,handler);
							HandshakeProtocol handshake = new HandshakeProtocol();
							handler.setDataHandler(handshake);
						}
						catch (Exception e)
						{
							System.err.println("Error registering socket - "+e.getMessage());
							e.printStackTrace();
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
					handlers.get((SocketChannel)sel.channel()).dataReceived();
					selector.selectedKeys().remove(sel);
				}
			}
		}
		System.out.println("Stopped listening");
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
