package com.blueprintit.firesync;

public class FireSync
{
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
