/**
 * @author Jens Buß, jens.buss@tu-dortmund.de
 * This package contains classes and processors to emulate the hardware trigger and trigger logic of FACT.
 *
 * It supports the following features of the real hardware trigger:
 *
 * - Discrimator (digitization of the analog signals in a patch)
 * - Conversion from DAC to millivolt forth and back
 * - Trigger Logic emulation
 *   - N-out-of-4 logic
 *   - N-out-of-40 logic
 *   - simple trigger coincidence for n == 1 in a n-out-of-4 and a n-out-of-40 logic
 * - Ratescans (scanning the trigger thresholds of the patches)
 *
 * The following features are known to be NOT YET implemented:
 *
 * - Coincidence of trigger signals of a n-out-of-4 and a n-out-of-40 logic for n > 1
 *      (It is not clear how this is realized in the hardware
 *
 * <ul>
 * <li>{@link fact.TriggerEmulation.Discriminator} is a class with functions to
 * discriminate the signal of a given patch (or a bunch of them). Furthermore it contains helper funtions
 * for the discrimination e.g. conversion between DAC and mV.
 * <li>{@link fact.TriggerEmulation.EmulateDiscriminator} is a processor that emulates
 * a discriminator that is working on the summed timeseries of patches. It uses functions from {@link fact.TriggerEmulation.Discriminator}
 * <li>{@link fact.TriggerEmulation.EmulateLogic} is a processor that emulates th n-out-of-4 trigger logic of FACT's
 * trigger unit and the n-out-of-40 logic of the trigger master by applying it to the array of trigger primitives compiled
 * by {@link fact.TriggerEmulation.EmulateDiscriminator}
 * <li>{@link fact.TriggerEmulation.Ratescan} is a processor that performs a software ratescan on summed patch time series
 * by iterating over a list of thresholds and applying {@link fact.TriggerEmulation.Discriminator} to the data
 * <li>{@link fact.TriggerEmulation.findMaximumTriggerThreshold}  is a processor that Scan the TriggerThreshold for each
 *  event in order to find the maximum possible threshold to keep the event by applying
 *  {@link fact.TriggerEmulation.Discriminator}.
 * <li>{@link fact.TriggerEmulation.SumUpPatches} lets you sum up the signals of each patch element wise and
 * returns an array of patchwise timeseries;
 * </ul>
 *
 *
 * <p>A typical process for a software trigger
 *
 * <pre>
 *
 * <fact.filter.ShapeSignal/>
 * <fact.TriggerEmulation.SumUpPatches/>
 * <fact.TriggerEmulation.EmulateDiscriminator/>
 * <fact.TriggerEmulation.EmulateLogic/>
 *
 * </pre>
 *
 * <p>A typical process for a software ratescan
 *
 * <pre>
 *
 * <fact.filter.ShapeSignal/>
 * <fact.TriggerEmulation.SumUpPatches/>
 * <fact.TriggerEmulation.Ratescan/>
 *
 * </pre>
 *
 * <p>A typical process for a max trigger search
 *  *
 *  * <pre>
 *  *
 *  * <fact.filter.ShapeSignal/>
 *  * <fact.TriggerEmulation.SumUpPatches/>
 *  * <fact.TriggerEmulation.findMaximumTriggerThreshold/>
 *  *
 *  * </pre>
 **/

package fact.TriggerEmulation;
