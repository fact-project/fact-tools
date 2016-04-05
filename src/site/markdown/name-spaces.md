# Introducing Hierarchical Name Spaces

The fact-tools are based on simple messages, each of which encodes an event
recorded by the telescope. The messages consist of a collection of (*key, value*)
pairs, each of which describes a property of the event.

As an example, the following table shows some of the keys available in each
data item (= message):

<div class="figure">
<table class="dataItem" style="margin: auto;">
	<tr>
		<th>Key</th>
		<th>Value</th>
	</tr>
	<tr><td>EventNum</td><td>3812</td></tr>
	<tr><td>NPIX</td><td>1440</td></tr>
	<tr><td>NROI</td><td>300</td></tr>
	<tr>
		<td>Data</td>
		<td>21,43,38,..</td>
	</tr>
</table>
<div class="caption" style="margin-top: 20px;">Figure 1: Outline of a data item.</div>
</div>

Keys are simple UTF-8 text strings and do not underlie any restrictions. For
the use of keys within the *FACT-Tools*, we propose a hierarchical naming scheme,
which eases the handling of messages and keys throughout the project.



### Naming Conventions

Before introducing the hierarchical name-space, we define a small set of conventions
that all keys should obey. The intention of these conventions is to find a reasonable
standardization of attribute names. The following rules shall be followed for any
keys:

  1. use lower-case letters and camel-case for word separation (all keys should start with lower-case)
  2. use colons `:` to separate different hierarchy levels

Valid examples for this naming conventions include

  - <span style="color: green;">`eventNum`</span>
  - <span style="color: green;">`pixels:arrivalTimes`</span>
  - <span style="color: green;">`pixels:arrivalTimes:mean`</span>

Some key strings that *do not* follow these conventions are:

  - <span style="color: red;">`pixels:Arrival_Time`</span>
  - <span style="color: red;">`pixels_arrival__time`</span>



## Hierarchical Name Spaces

With a more standardized scheme for keys, an useful property of keys is a way to group elements that
somehow belong together. For this purpose, we propose a hierarchical name space scheme, which uses
colons `:` in the keys to derive a tree of names. This allows for using regular expressions or simple
wildcard pattern matching to quickly select or de-select elements that are not used or required in
a specific context.

Using a wildcard pattern like `pixels:*` allows for selecting all pixel-related attributes. With
the pattern `pixels:*:mean`, we can easily select all `mean` values for all properties computed
under the `pixels:` prefix.


### FACT Tools Key Hierarchy

For the FACT data, we define a couple of top-level prefixes, that span the name space used within
the FACT tools. The following top-level elements are used:

   - `meta` - related to any meta-data (event-id, recording time, region-of-interest)
   - `mc` - all meta-information produced within MC simulations (true energy,...)
   - `pixels` - the pixel related data, i.e. all the arrays of length 1440 containing pixel information (arrivalTimes,...)
   - `shower` - the information computed based on the shower pixels (ellipse, charges,...)
   - `pedestal` - information related to pedestal runs
   - `ring` - attributes computed by muon-ring processing

For compatibility reasons, the attributes, read from FITS files are additionally included in the messages 
by their original, unmodified name.

The following figure shows the name space tree spanned by some of the attributes used within the
FACT Tools. This figure is an example, including some mock-up elements.

<div class="figure">
	<img src="../images/name-space-tree.png" style="width: 600px;" />
	<div class="caption">Figure 2: Example name space tree.</div>
</div>