/**
 * This class is generated by jOOQ
 */
package fact.rta.persistence.tables.records;


import fact.rta.persistence.tables.Signal;

import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.7.2"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class SignalRecord extends UpdatableRecordImpl<SignalRecord> implements Record4<Timestamp, Integer, Integer, Float> {

	private static final long serialVersionUID = 1513390235;

	/**
	 * Setter for <code>signal.timestamp</code>.
	 */
	public void setTimestamp(Timestamp value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>signal.timestamp</code>.
	 */
	public Timestamp getTimestamp() {
		return (Timestamp) getValue(0);
	}

	/**
	 * Setter for <code>signal.signal</code>.
	 */
	public void setSignal(Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>signal.signal</code>.
	 */
	public Integer getSignal() {
		return (Integer) getValue(1);
	}

	/**
	 * Setter for <code>signal.background</code>.
	 */
	public void setBackground(Integer value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>signal.background</code>.
	 */
	public Integer getBackground() {
		return (Integer) getValue(2);
	}

	/**
	 * Setter for <code>signal.duration_in_seconds</code>.
	 */
	public void setDurationInSeconds(Float value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>signal.duration_in_seconds</code>.
	 */
	public Float getDurationInSeconds() {
		return (Float) getValue(3);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Record1<Timestamp> key() {
		return (Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record4 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row4<Timestamp, Integer, Integer, Float> fieldsRow() {
		return (Row4) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row4<Timestamp, Integer, Integer, Float> valuesRow() {
		return (Row4) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Timestamp> field1() {
		return Signal.SIGNAL.TIMESTAMP;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field2() {
		return Signal.SIGNAL.SIGNAL_;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field3() {
		return Signal.SIGNAL.BACKGROUND;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Float> field4() {
		return Signal.SIGNAL.DURATION_IN_SECONDS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Timestamp value1() {
		return getTimestamp();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value2() {
		return getSignal();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value3() {
		return getBackground();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Float value4() {
		return getDurationInSeconds();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SignalRecord value1(Timestamp value) {
		setTimestamp(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SignalRecord value2(Integer value) {
		setSignal(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SignalRecord value3(Integer value) {
		setBackground(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SignalRecord value4(Float value) {
		setDurationInSeconds(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SignalRecord values(Timestamp value1, Integer value2, Integer value3, Float value4) {
		value1(value1);
		value2(value2);
		value3(value3);
		value4(value4);
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached SignalRecord
	 */
	public SignalRecord() {
		super(Signal.SIGNAL);
	}

	/**
	 * Create a detached, initialised SignalRecord
	 */
	public SignalRecord(Timestamp timestamp, Integer signal, Integer background, Float durationInSeconds) {
		super(Signal.SIGNAL);

		setValue(0, timestamp);
		setValue(1, signal);
		setValue(2, background);
		setValue(3, durationInSeconds);
	}
}
