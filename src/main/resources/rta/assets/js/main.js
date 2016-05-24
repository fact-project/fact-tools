/*
	Prism by TEMPLATED
	templated.co @templatedco
	Released for free under the Creative Commons Attribution 3.0 license (templated.co/license)
*/

//
const $ = require('zeptojs');
const Hexmap = require('hexmap');
const numeral = require('numeral');
const c3 = require('c3');
const _ = require('lodash/core')


//(function($) {
//
//	skel.breakpoints({
//		xlarge:	'(max-width: 1680px)',
//		large:	'(max-width: 1280px)',
//		medium:	'(max-width: 980px)',
//		small:	'(max-width: 736px)',
//		xsmall:	'(max-width: 480px)'
//	});
//
//	$(function() {
//
//		var	$window = $(window),
//			$body = $('body');
//
//		// Disable animations/transitions until the page has loaded.
//			$body.addClass('is-loading');
//
//			$window.on('load', function() {
//				window.setTimeout(function() {
//					$body.removeClass('is-loading');
//				}, 100);
//			});
//
//		// Fix: Placeholder polyfill.
//			$('form').placeholder();
//
//		// Prioritize "important" elements on medium.
//			skel.on('+medium -medium', function() {
//				$.prioritize(
//					'.important\\28 medium\\29',
//					skel.breakpoint('medium').active
//				);
//			});
//
//	});
//
//})(jQuery);

/*
FACT RTA stuff
 */

SECONDS = 1000;
MINUTES = 60*SECONDS;


$(document).ready(init);


function generateChart(){
    var chart = c3.generate({
        data: {
            x: 'x',
            columns: [
                ['x', '2012-12-29', '2012-12-30', '2012-12-31'],
                ['data1', 230, 300, 330],
                ['data2', 190, 230, 200],
                ['data3', 90, 130, 180]
            ]
        },
        axis: {
            x: {
                type: 'timeseries',
                tick: {
                    format: '%M/%s'
                }
            }
        }
    });
}

function init() {
    //initialize the hex display
    var parentID = 'fact_map';
    var size = 450;
    var radius = 5;
    var camera = new Hexmap(parentID, size, radius);

    function loadSkyCamImage() {
        console.log("loading allskycam iamge")
        $("#allskycam").attr("width", "200");
        $("#allskycam").attr("src", "./images/hex-loader2.gif")
        setTimeout(function () {
            console.log("loading allskycam iamg inner ")
            d = new Date();
            $("#allskycam").attr("width", " ");
            $("#allskycam").attr("src", "http://www.gtc.iac.es/multimedia/netcam/camaraAllSky.jpg?" + d.getTime());
        }, 4000);
    }


    function loadEvent() {
        console.log("loading Event")

        $.getJSON('/event', function (latestEvent) {
            if (latestEvent) {
                camera.update(latestEvent.photonCharges, duration = 2.0);
                $('#source_name').html(latestEvent.sourceName);
                $('#eventTimeStamp').html(latestEvent.eventTimeStamp);
                $('#size').html(numeral(latestEvent.size).format('0.00'));
                $('#energy').html(numeral(latestEvent.estimatedEnergy).format('0.00'));
                $('#theta_square').html(numeral(latestEvent.thetaSquare).format('0.000'));
            }
        });
    }

    function LightCurve() {
        console.log("loading LC")

        $.getJSON('/lightcurve', function (lightcurve) {
            if (lightcurve) {
                $('#lightcurve').html(lightcurve);
            }
        });

        function refresh(){
            console.log("loading LC")
            $.getJSON('/lightcurve', function (lightcurve) {
                console.log(lightcurve);
                if (lightcurve) {
                    $('#lightcurve').html(lightcurve);
                }
            });
        }

        LightCurve.refresh = refresh;
    }


    function MemoryChart() {

        var MB = 1/(1024*1024);
        var GB = 1/(1024*1024*1024);
        var chart = null;
        var latestEntry = null;

        var refreshDisplay = function (latestEntry){
            latestStatus = latestEntry.value;
            $('#space').html(numeral(latestStatus.freeSpace * GB).format('0.0'));
            $('#memory').html(numeral(latestStatus.usedMemory * MB).format('0.0'));
            $('#cpus').html(latestStatus.availableProcessors);
        };

        var parseDataToColumns = function (status_dict){
            var rs = _.values(status_dict);
            var latestStatus = rs[rs.length - 1];
            rs  = _.map(rs, 'usedMemory');
            rs = _.map(rs, function(r){
                return r*MB;
            });
            rs = ['usedMemory'].concat(rs);

            var ts  = _.keys(status_dict);
            latestTimeStamp = ts[ts.length - 1];
            ts = ['t'].concat(ts);
            return {"time":ts, "value":rs};
        };

        var  getLatestEntry = function (status_dict){
            var rs = _.values(status_dict);
            var latestStatus = rs[rs.length - 1];

            var ts  = _.keys(status_dict);
            latestTimeStamp = ts[ts.length - 1];
            return {"time":latestTimeStamp, "value":latestStatus};
        };

        $.getJSON('/status', function (status_dict) {
            latestEntry = getLatestEntry(status_dict);
            refreshDisplay(latestEntry);

            var columns = parseDataToColumns(status_dict);
            chart = c3.generate({
                size: {
                    height: 240,
                    width: 480
                },
                bindto: '#memory_chart',
                data: {
                    x: 't',
                    xFormat: '%Y-%m-%dT%H:%M:%S.%L',
                    y: 'usedMemory',
                    columns:[
                        columns.time,
                        columns.value
                    ],
                    type:'bar'
                },
                bar: {
                    width: {
                        ratio: 1
                    }
                },
                axis: {
                    x: {
                        type: 'timeseries',
                        tick: {
                            format: '%H:%M:%S'
                        }
                    }
                }
            });
        });


        function load(){
            if(chart) {
                console.log("request  " +  '/status?timestamp='+latestTimeStamp)
                $.getJSON('/status?timestamp='+latestTimeStamp, function (status_dict) {
                    if (status_dict) {

                        latestEntry = getLatestEntry(status_dict);
                        refreshDisplay(latestEntry);

                        var column = parseDataToColumns(status_dict);

                        chart.flow({
                            columns: [
                                column.value,
                                column.time
                            ]
                        });
                    }
                });
            } else{
                console.log("Error. chart not loaded.")
            }
        }

        MemoryChart.load = load;
    }





    function DataRateChart(){
        var chart = null;
        var latestTimeStamp = null;

        $.getJSON('/datarate', function(rates){

            rs  = _.values(rates);
            var currentRate = rs[rs.length - 1];
            rs = ['rate'].concat(rs);

            ts  = _.keys(rates);
            latestTimeStamp = ts[ts.length - 1];
            ts = ['t'].concat(ts);


            console.log(rs);
            console.log(ts);
            chart = c3.generate({
                size: {
                    height: 240,
                    width: 480
                },
                bindto: '#datarate_chart',
                data: {
                    x: 't',
                    xFormat: '%Y-%m-%dT%H:%M:%S.%L',
                    y: 'rate',
                    columns:[
                        ts,
                        rs
                    ]
                },
                axis: {
                    x: {
                        count: 100,
                        type: 'timeseries',
                        tick: {
                            format: '%H:%M:%S'
                        }
                    }
                }
            });
        });


        function load(){
            if(chart) {
                $.getJSON('/datarate?timestamp='+latestTimeStamp, function (rates) {
                    if (rates) {
                        var rs  = _.values(rates);
                        var currentRate = rs[rs.length - 1];
                        rs = ['rate'].concat(rs);

                        var ts  = _.keys(rates)
                        latestTimeStamp = ts[ts.length - 1];
                        ts = ['t'].concat(ts);

                        $('#datarate').html(numeral(currentRate).format('0.0'));

                        chart.flow({
                            columns: [
                                ts,
                                rs
                            ]
                        });
                    }
                });
            } else{
                console.log("Error. chart not loaded.")
            }
        }
        DataRateChart.load = load;
    }



    DataRateChart();
    window.setInterval(DataRateChart.load, 2*SECONDS);

    loadSkyCamImage();
    window.setInterval(loadSkyCamImage, 3*MINUTES);
    //
    loadEvent();
    window.setInterval(loadEvent, 15*SECONDS);
    //
    MemoryChart();
    window.setInterval(MemoryChart.load, 30*SECONDS);

    LightCurve();
    window.setInterval(LightCurve.refresh, 10*SECONDS);

}