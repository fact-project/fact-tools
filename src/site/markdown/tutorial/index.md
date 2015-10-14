# FACT Tools Tutorial

This tutorial provides an introduction to the FACT tools for the preprocessing
of telescope data. The FACT tools cover the processing steps from raw camera-level
to the image cleaning and extraction of image parameters (Hillas parameters).



### Software Requirements

The FACT Tools are written in Java, which allows their execution on any system
with a Java virtual machine. The binary jar file is self-contained and does not
require any pre-installation.

The following list shows the requirements for the tutorial hands-on session:

  1. A recent Java Runtime environment (Java 1.7 or higher). OpenJDK is working
     fine and can easily be installed on CentOS/Ubuntu machines using the
     package management.

  2. The [latest FACT-Tools binary](../download.html) file.



### Supplementary Material

* [slides for the turorial][sexten-slides] held during the CTA School in Sesto/Sexten (July, 2015)

* several example configuration files
	* [example 01][example1]: simply print data
	* [example 02][example2]: show viewer
	* [example 03][example3]: drs calibration with viewer
	* [example 04][example4]: drs calibration + smoothing
	* ... further examples can be simply produced following the slides

[sexten-slides]:https://docs.google.com/presentation/d/18zTy3s0lEZsAStXsKdJBpP0KYAptmWDyOvxfBa9eKII/pub?start=false&loop=false&delayms=5000
[example1]: https://drive.google.com/file/d/0BxYOS1sFQ5cwbmZkd0dWZmdaUUE/view?usp=sharing
[example2]: https://drive.google.com/file/d/0BxYOS1sFQ5cwRDN2MUczSXFCOVU/view?usp=sharing
[example3]: https://drive.google.com/file/d/0BxYOS1sFQ5cwcldnNWdZeXpQYUE/view?usp=sharing
[example4]: https://drive.google.com/file/d/0BxYOS1sFQ5cwalBhd1dkUkhJcVE/view?usp=sharing