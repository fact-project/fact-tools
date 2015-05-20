##Logging
A convinient way to produce output with different verbosity levels is to use the slf4j logging library. 
Its already included as a maven dependency so you dont have to worry about it. Simply import these two classes

		import org.slf4j.Logger;
		import org.slf4j.LoggerFactory;

and you're good to go.

		final Logger log = LoggerFactory.getLogger(YourProcessor.class);

		//for very verbose output that helps you debug your code
		log.debug("message");

		//in case an error happens. usually means you want to cancel the stream and inform the user of the error
		log.error("message");

		//display some useful information to the user
		log.info("message");

		// a warning message. For recovarable errors and such.
		log.warn("message");

You'll find the settings to controll the logging output in
		
		/facttools/src/main/resources/log4j.properties

Some more information about these settings can be found [here](http://slf4j.org/manual.html)


