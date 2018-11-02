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
 *
 *
 * Minimal example: Superposition process:
 * ======================================
 *
 * <pre>
 * <fact.datacorrection.DrsCalibration
 *                  key="Data"
 *                  outputKey="DataCalibrated"/>
 *
 * <fact.pedestalSuperpostion.SamplePedestalEvent
 *                  prependKey="LONS_"
 *                  noiseDatabase="./noiseDB.json"
 *                  dataFolder="/fact/raw"
 *                  dbBinningKey="Zd"
 *                  itemBinningKey="MPointingPos.fZd"
 *                  binning="2" />
 *
 * <fact.datacorrection.DrsCalibration
 *                  drsKey="LONS_drspath"
 *                  key="LONS_Data"
 *                  outputKey="LONS_DataCalibrated" />
 *
 * <fact.utils.CombineDataArrays
 *                  firstArrayKey="DataCalibrated"
 *                  secondArrayKey="LONS_DataCalibrated"
 *                  outputKey="DataCalibrated"
 *                  op="add" />
 *
 * <fact.datacorrection.DrsCalibration
 *                  key="DataCalibrated"
 *                  outputKey="Data"
 *                  reverse="true"
 * />
 * <fact.utils.Remapping
 *                  key="Data"
 *                  outputKey="Data"
 *                  reverse="True"
 * />
 *</pre>
 **/
