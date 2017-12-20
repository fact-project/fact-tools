/**
 * This package contains a number of classes which are supposed to display some (visual) information about the current state of the stream.
 * The __ScatterPlotter__ for example opens up a window with a 2D ScatterPlot for the given x and y values.
 * The plotters in this package provide a convenient way to quickly analyze the semantics of the ongoing stream.
 * These classes should make it possible to quickly get an overview of the streamed and processed data.
 * However these classes are_not_meant to provide the same feature set as a real scientific plotting application like  GNUPLot.
 * Most plotters in this package extend the__DataVisualizer__class from the Streams-Framework and rely on the __JFreeChart__ library for graphical output.
 */
package fact.plotter;
