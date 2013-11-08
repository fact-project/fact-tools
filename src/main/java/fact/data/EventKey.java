/**
 * 
 */
package fact.data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;

/**
 * @author chris
 * 
 */
public class EventKey implements Serializable, Processor {

	/** The unique class ID */
	private static final long serialVersionUID = 7195966755073128065L;

	final static SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");
	static Logger log = LoggerFactory.getLogger(EventKey.class);
	Date date;
	Integer run;
	Integer id;
	Set<String> tags = new TreeSet<String>();

	public EventKey() {
	}

	public EventKey(Date date, Integer run, Integer id) {
		this.date = date;
		this.run = run;
		this.id = id;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @return the run
	 */
	public Integer getRun() {
		return run;
	}

	/**
	 * @param run
	 *            the run to set
	 */
	public void setRun(Integer run) {
		this.run = run;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	public String toString() {
		return fmt.format(date) + "/" + run + "/" + id;
	}

	/**
	 * @return the tags
	 */
	public Set<String> getTags() {

		if (tags == null) {
			tags = new TreeSet<String>();
		}

		return tags;
	}

	public boolean equals(Object o) {
		return toString().equals(o.toString());
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public static EventKey parse(String str) throws Exception {

		String dateString = str.substring(0, 10);
		Date date = fmt.parse(dateString);
		log.debug("Date is: {}", date);

		String rest = str.substring(dateString.length() + 1);
		log.debug("Run/Event is {}", rest);

		String tok[] = rest.split("/");
		Integer run = new Integer(tok[0]);
		Integer event = new Integer(tok[1]);
		log.debug("Run is: {}, Event is: {}", run, event);
		return new EventKey(date, run, event);
	}

	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data data) {

		try {
			log.info("Creating key for {}", data);
			int[] utc = (int[]) data.get("UnixTimeUTC");
			Date date = new Date(utc[0] * 1000L);
			Integer num = new Integer(data.get("EventNum").toString());

			EventKey key = new EventKey(date, -1, num);
			log.info("EventKey is: {}", key.toString());
			data.put("@id", key);
		} catch (Exception e) {
			log.error("Error: " + e.getMessage());
		}

		return data;
	}


}
