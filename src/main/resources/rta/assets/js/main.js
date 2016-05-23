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
                ['data3', 90, 130, 180],
            ]
        },
        axis: {
            x: {
                type: 'timeseries',
                tick: {
                    format: '%M/%s',
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
    this.camera = new Hexmap(parentID, size, radius);

    function loadSkyCamImage() {
        console.log("loading allskycam iamge")
        $("#allskycam").attr("width", "400");
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

        $.getJSON('/event', function (event) {
            if (event) {
                console.log(event)
                this.camera.update(event.photonCharges, duration = 1);
                $('#source_name').html(event.sourceName);
                $('#eventTimeStamp').html(event.eventTimeStamp);
                $('#size').html(numeral(event.size).format('0.00'));
                $('#energy').html(numeral(event.estimatedEnergy).format('0.00'));
                $('#theta_square').html(numeral(event.thetaSquare).format('0.000'));
            }
        });
    }


    function MemoryChart() {
        var chart;
        $.getJSON('/status/all', function (statuslist) {

            rs = _.map(statuslist, 'usedMemory');
            rs = ['memory'].concat(rs);

            ts = _.map(statuslist, 'timeStamp');
            ts = ['t'].concat(ts);

            console.log(rs);
            console.log(ts);
            chart = c3.generate({
                size: {
                    height: 240,
                    width: 480
                },
                bindto: '#memory_chart',
                data: {
                    x: 't',
                    xFormat: '%Y-%m-%dT%H:%M:%S.%L',
                    y: 'memory',
                    columns:[
                        ts,
                        rs
                    ],
                    type:'bar',
                },
                bar: {
                    width: {
                        ratio: 1 // this makes bar width 50% of length between ticks
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
        function load() {
            $.getJSON('/status', function (status) {
                if (status) {
                    console.log(status);
                    $('#space').html(status.freeSpace);
                    $('#memory').html(status.usedMemory);
                    $('#cpus').html(status.availableProcessors);

                    chart.flow({
                        columns: [
                            ['t', status.timeStamp],
                            ["memory", status.usedMemory]
                        ]
                    });
                }
            });
        }

        MemoryChart.load = load;
    }





    function DataRateChart(){
        var chart = null;
        var latestTimeStamp = null;

        $.getJSON('/datarate', function(rates){

            rs  = _.map(rates, 'rate');
            rs = ['rate'].concat(rs);

            ts  = _.map(rates, 'timeStamp');
            latestTimeStamp = ts[0];
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
                console.log("request  " +  '/datarate?timestamp='+latestTimeStamp)
                $.getJSON('/datarate?timestamp='+latestTimeStamp, function (rates) {
                    if (rates) {
                        console.log(rates)
                        rs  = _.map(rates, 'rate');
                        var currentRate = rs[rs.length - 1]
                        rs = ['rate'].concat(rs);

                        ts  = _.map(rates, 'timeStamp');
                        latestTimeStamp = ts[ts.length - 1];
                        ts = ['t'].concat(ts);
                        console.log(ts)
                        $('#datarate').html(numeral(currentRate).format('0.0'));
                        // console.log(rate);
                        chart.flow({
                            columns: [
                                ts,
                                rs
                            ]
                            // to: '13:25:55',
                            // duration: 1000,
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

    loadEvent();
    window.setInterval(loadEvent, 15*SECONDS);

    MemoryChart();
    window.setInterval(MemoryChart.load, 30*SECONDS);

}