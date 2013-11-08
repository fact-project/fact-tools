/**
 * 
 */
package fact.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

import stream.Data;
import stream.Processor;

/**
 * @author chris
 * 
 */
public class Database implements Processor {

	Connection con;

	public Database(String table, Map<String, Class<?>> types, String jdbc,
			String user, String password) throws Exception {
		con = DriverManager.getConnection(jdbc, user, password);
	}

	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data data) {
		return null;
	}
}