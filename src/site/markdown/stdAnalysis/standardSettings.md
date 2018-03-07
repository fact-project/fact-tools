# Standard Settings

The settings for the standard analysis are defined in the classpath:/default/settings.properties (respectively classpath:/default/settings_mc.properties) files. (To remember the classpath can be found under src/main/resources)
They can be used in any xml file, with the following line:

    <properties url="classpath:/default/settings.properties" />

Each property can be overwritten by specifing the defintion of the property later:

    [...]
    <properties url="classpath:/default/settings_mc.properties" />
    <property name="TwoLevelTimeNeighbor.coreThreshold" value="5.0" />
    [...]

Now follows the specification of the settings:

<style>
    table,th,td {
        border: 1px solid black;
        /*border-collapse: collapse;*/
    }
    th,td  {
        padding: 5px;
    }
</style>

<table style="width:100%">
    <tr> 
        <th> Name </th>
        <th> Description </th>
        <th> Used in the MCs? </th>
    </tr>
    <tr> 
        <td colspan="3"> Calibration Settings </td>
    </tr>
    <tr> 
        <td> prevEvents_limitEvents </td>
        <td> Maximum number of startcell arrays of previous events are stored (and used for the jump removal) </td>
        <td> False </td>
    </tr>
    <tr> 
        <td> patchJumpRemoval_jumpLimit </td>
        <td> Minimal voltage difference for considering if there is a jump </td>   
        <td> False </td>
    </tr>
    <tr> 
        <td> removeSpikes_leftBorder </td>
        <td> Left border of the window in the roi in which spikes are removed </td>
        <td> True </td>
    </tr>
    <tr>
        <td> removeSpikes_spikeLimit </td>
        <td> Minimal voltage difference at the edge of spikes </td>  
        <td> True </td>
    </tr>
    <tr>
        <td> removeSpikes_topSlopeLimit </td>
        <td> Maximal voltage difference at the top of spikes </td> 
        <td> True </td>
    </tr>
    <tr>
        <td> removeSpikes_maxSpikeLength </td>
        <td> Maximal lenght of spikes </td>
        <td> True </td>
    </tr>
    <tr>
        <td> interpolateBadPixel_badChidIDs </td>
        <td> chid ids of bad pixels, which will be interpolated   </td>
        <td> True </td>
    </tr>
    <tr>
        <td colspan="3"> Extraction Settings </td>
    </tr>
    <tr>
        <td> basicExtraction_startSearchWindow </td>
        <td> Left border of the search window for the max amplitude of the pulse </td>
        <td> True </td>
    </tr>
    <tr>
        <td> basicExtraction_rangeSearchWindow </td>
        <td> Range of the search window for the max amplitude of the pulse </td> 
        <td> True </td>
    </tr>
    <tr>
        <td> basicExtraction_rangeHalfHeigthWindow </td>
        <td> Range of the search window in front of the max amplitude for the half height of the pulse </td>
        <td> True </td>
    </tr>
    <tr>
        <td> risingEdgePolynomFit_numberOfPoints </td>
        <td> Number of points used for the polynom fit, to determine the rising edge of the pulse </td>  
        <td> True </td>
    </tr>
    <tr>
        <td colspan="3"> Cleaning Settings </td>
    </tr>
    <tr>
        <td> twoLevelTimeNeighbor_coreThreshold </td>
        <td> threshold for identifying core pixels </td>
        <td> True </td>
    </tr>
    <tr>
        <td> twoLevelTimeNeighbor_neighborThreshold </td>
        <td> threshold for identifying neighbor pixels </td>
        <td> True </td>
    </tr>
    <tr>
        <td> twoLevelTimeNeighbor_timeLimit </td>
        <td> limit for the time difference between neighboring pixels     </td>
        <td> True </td>
    </tr>
    <tr>
        <td> twoLevelTimeNeighbor_minNumberOfPixel </td>
        <td> Minimum number of pixels in a cluster    </td>
        <td> True </td>
    </tr>
    <tr>
        <td colspan="3"> Outputkeys Settings </td>
    </tr>
    <tr>
        <td> event_param </td>
        <td> Outputkeys describing the event      </td>
        <td> False </td>
    </tr>
    <tr>
        <td> tracking_param </td>
        <td> Outputkeys describing the drive parameters   </td>
        <td> True </td>
    </tr>
    <tr>
        <td> stat* </td>
        <td> Outputkeys, describing several stastical features of the pulse extraction    </td>
        <td> True </td>
    </tr>
    <tr>
        <td> shower_param </td>
        <td> Outputkeys describing the identified shower      </td>
        <td> True </td>
    </tr>
    <tr>
        <td> conc_param </td>
        <td> Outputkeys describing concentration parameters   </td>
        <td> True </td>
    </tr>
    <tr>
        <td> spread_param </td>
        <td> Outputkeys describing spread parameters   </td>
        <td> True </td>
    </tr>
    <tr>
        <td> time_gradient </td>
        <td> Outputkeys describing the time gradient fit   </td>
        <td> True </td>
    </tr>
    <tr>
        <td> sourcePos </td>
        <td> Outputkeys for the sourcePositions   </td>
        <td> True </td>
    </tr>
    <tr>
        <td> alpha </td>
        <td> Outputkeys Alpha   </td>
        <td> True </td>
    </tr>
    <tr>
        <td> distance </td>
        <td> Outputkeys distance   </td>
        <td> True </td>
    </tr>
    <tr>
        <td> cosdeltaalpha </td>
        <td> Outputkeys cosdeltaalpha   </td>
        <td> True </td>
    </tr>
    <tr>
        <td> theta </td>
        <td> Outputkeys theta   </td>
        <td> True </td>
    </tr>
    <tr>
        <td> keysForOutput </td>
        <td> Combination of all outputkeys   </td>
        <td> True </td>
    </tr>
</table>
