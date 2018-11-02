package fact.pedestalSuperposition;

/**
 * @author michael bulinski, michael.bulinski@tu-dortmund.de; Jens Buß, jens.buss@tu-dortmund.de
 * This package contains a processors to sample and superimpose pedestal events with events from
 * the input stream e.g. shower simulations without noise simulation.
 *
 * The processor <li>{@link fact.pedestalSuperposition.SamplePedestalEvent} loads a given noise database
 * and randomly samples a pedestal event and appends the event and its drs file to the data item.
 *
 * Noise DB:
 * =========
 *
 * The Noise DB is a json wile with the columns the mandatory columns:
 *  - eventNr (position of the event in the fits file belonging to the cobination of NIGHT and RUNID)
 *  - NIGHT
 *  - RUNID
 *  - drs0 (next drs file before the fits file belonging to the cobination of eventNr, NIGHT, RUNID)
 *  - drs1 (next drs file after the fits file belonging to the cobination of eventNr, NIGHT, RUNID)
 *  - feature column for the dbBinningKey used by <li>{@link fact.pedestalSuperposition.SamplePedestalEvent}
 *    e.g. currents, Zd, moonZdDist
 *
 * In order to create such a noise DB an event index of recorded (pedestal) events is required. This index should
 * contain the features listed above. The noise DB is an excerpt of this index that is holding for a certain set of
 * conditions.
 * A collection of methods to generate such an index and a noise db file,
 * can be found at: https://github.com/fact-project/EventList
 **/
