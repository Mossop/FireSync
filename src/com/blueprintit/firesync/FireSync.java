package com.blueprintit.firesync;

import org.apache.commons.cli.*;

/**
	* The entry point for firesync. Generally used for the client version
	* passing a --server option will instatiate the server version instead.
	*/
public class FireSync
{
	/**
		* Builds an option list for CLI to parse for.
		*/
	private static Options buildOptions()
	{
		Options options = new Options();
		
		options.addOption("nr","nonrecursive",false,"Instructs the firesync to only include the current directory");
		options.addOption("h","help",false,"Displays help for the command line");

		return options;
	}
	
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
			Options opt = buildOptions();
			try
			{
				CommandLine cl = (new GnuParser()).parse(opt,args);
				System.out.println("Found options");
				for (Option o : cl.getOptions())
				{
					System.out.println(o);
				}
				System.out.println("Unknown items");
				for (String s : cl.getArgs())
				{
					System.out.println(s);
				}
				System.out.println();
				if (cl.hasOption("help"))
				{
					(new HelpFormatter()).printHelp("FireSync firesync://server[:port]/path/to/files",opt,true);
				}
				else
				{
				}
			}
			catch (Exception e)
			{
				System.err.println("Unable to parse command line: "+e.getMessage());
			}
		}
	}
}
