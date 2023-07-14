package io.split.dbm.logLevelChanger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import io.split.client.SplitClient;
import io.split.client.SplitClientConfig;
import io.split.client.SplitFactoryBuilder;
import io.split.client.api.SplitResult;

/**
 * Hello world!
 *
 */
public class App 
{
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(App.class.getName());
	static SplitClient splitSdk;

	public static void main( String[] args ) throws Exception
	{
		System.out.println( "Hello App!" );
		logger.info("Hello Logger!");
		SplitClientConfig config = SplitClientConfig.builder()
				.setBlockUntilReadyTimeout(5000)
				.build();

		splitSdk = SplitFactoryBuilder.build("<your split server-side api key here>", config).client();
		splitSdk.blockUntilReady();
		logger.info("split sdk is ready!");

		new App().execute();
	}

	String level;

	public void
	execute() {
		System.out.println("execute!");
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

		Runnable task = new Runnable() {
			public void run() {
				logger.info("info dbm");
				logger.warning("warning dbm2");
			}
		};

		executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);   

		Runnable splitTask = new Runnable() {
			public void run() {
				SplitResult result = splitSdk.getTreatmentWithConfig("<YOUR USER_ID>", "log_level");
				JSONObject jsonObj = new JSONObject(result.config());
				level = jsonObj.getString("log_level");
				Field[] fields = java.util.logging.Level.class.getFields();
				try {
					Method setLevelMethod = java.util.logging.Logger.class.getMethod("setLevel", java.util.logging.Level.class);
					for(Field f : fields) {
						if(f.getName().equalsIgnoreCase(level)) {
							System.out.println("setting to log level: " + f.getName());
							setLevelMethod.invoke(logger, f.get(null));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		executor.scheduleAtFixedRate(splitTask, 0, 1, TimeUnit.SECONDS);
	}
}
