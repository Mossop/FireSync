package com.blueprintit.firesync;

/**
	* The entry point for firesync. Generally used for the client version
	* passing a --server option will instatiate the server version instead.
	*/
public class FireSync
{
	/**
		* Entry point for the application. Parses arguments and starts the client up.
		*/
	public static void main(String[] args)
	{
		boolean asServer = false;
		for(int loop=0; loop<args.length; loop++)
		{
			if (args[loop].equalsIgnoreCase("--server"))
			{
				asServer=true;
			}
		}
		if (asServer)
		{
			FireSyncServer.main(args);
		}
		else
		{
		}
	}
}
