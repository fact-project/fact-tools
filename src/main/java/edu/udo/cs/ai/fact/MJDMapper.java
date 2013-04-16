/**
 * 
 */
package edu.udo.cs.ai.fact;

import java.io.Serializable;
import java.util.Date;

import stream.Processor;
import stream.Data;

/**
 * @author chris
 * 
 */
public class MJDMapper implements Processor {
	final static double JD_MJD_DIFF = 2400000.5d;
	final static double UNIX_JD_OFFSET = 2440587.5;

	String key;
	String target;

	public MJDMapper() {
	}

	public MJDMapper(String key) {
		this.key = key;
	}

	public MJDMapper(String key, String target) {
		this.key = key;
		this.target = target;
	}

	public static Long mjd2unixtime(Double mjd) {
		double jd = mjd + JD_MJD_DIFF;
		Double unix = (jd - UNIX_JD_OFFSET) * 86400 * 1000;
		return unix.longValue();
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	public Data process(Data data) {

		if (key == null)
			return data;

		Serializable ser = data.get(key);
		if (ser == null)
			return data;

		Double mjd = new Double(ser.toString());

		if (target == null) {
			target = "unixtime(" + key + ")";
		}

		data.put(target, mjd2unixtime(mjd));
		return data;
	}

	public static void main(String[] args) {
		Double in = 55886.248777824076;
		in = 55887.64865;
		in = 49987.0;
		Long out = mjd2unixtime(in);
		System.out.println("in: " + in);
		System.out.println("unix: " + out);
		System.out.println("Date: " + new Date(out));

		System.out.println("exp: " + "1321976043");
	}
}