/**
 * This package is supposed to be a collection of convinience processors to help calculate some statistical values.
 * For example the ArrayRMS, ArrayMean, ArrayVariance etc. processors take an array as input and put a single Double value back into the getColorFromValue.
 * The ArrayElementsMean processor puts out an array of the same length as the input. Each element containing the accumulated mean value of the input.
 * <p>
 * This should NOT replace analysis software like ssps or root. This is meant to get a quick overview of the data thats currently being analyzed.
 * However this package also contains some useful processors to calculate the Hillas Parameter;
 */
package fact.statistics;
