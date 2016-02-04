/**
 * This class is generated by jOOQ
 */
package fact.rta.persistence.tables;


import fact.rta.persistence.DefaultSchema;
import fact.rta.persistence.Keys;
import fact.rta.persistence.tables.records.SignalRecord;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;


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
public class Signal extends TableImpl<SignalRecord> {

	private static final long serialVersionUID = -659816843;

	/**
	 * The reference instance of <code>signal</code>
	 */
	public static final Signal SIGNAL = new Signal();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<SignalRecord> getRecordType() {
		return SignalRecord.class;
	}

	/**
	 * The column <code>signal.timestamp</code>.
	 */
	public final TableField<SignalRecord, Timestamp> TIMESTAMP = createField("timestamp", org.jooq.impl.SQLDataType.TIMESTAMP, this, "");

	/**
	 * The column <code>signal.signal</code>.
	 */
	public final TableField<SignalRecord, Integer> SIGNAL_ = createField("signal", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>signal.background</code>.
	 */
	public final TableField<SignalRecord, Integer> BACKGROUND = createField("background", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>signal.duration_in_seconds</code>.
	 */
	public final TableField<SignalRecord, Float> DURATION_IN_SECONDS = createField("duration_in_seconds", org.jooq.impl.SQLDataType.REAL, this, "");

	/**
	 * Create a <code>signal</code> table reference
	 */
	public Signal() {
		this("signal", null);
	}

	/**
	 * Create an aliased <code>signal</code> table reference
	 */
	public Signal(String alias) {
		this(alias, SIGNAL);
	}

	private Signal(String alias, Table<SignalRecord> aliased) {
		this(alias, aliased, null);
	}

	private Signal(String alias, Table<SignalRecord> aliased, Field<?>[] parameters) {
		super(alias, DefaultSchema.DEFAULT_SCHEMA, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<SignalRecord> getPrimaryKey() {
		return Keys.PK_SIGNAL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<SignalRecord>> getKeys() {
		return Arrays.<UniqueKey<SignalRecord>>asList(Keys.PK_SIGNAL);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Signal as(String alias) {
		return new Signal(alias, this);
	}

	/**
	 * Rename this table
	 */
	public Signal rename(String name) {
		return new Signal(name, null);
	}
}
