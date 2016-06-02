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
const _ = require('lodash/core');
const d3_time = require('d3-time');
const d3 = require('d3');


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

function init() {
    //initialize the hex display
    var parentID = 'fact_map';
    var size = 450;
    var radius = 5;
    var camera = new Hexmap(parentID, size, radius);

    function loadSkyCamImage() {
        console.log("loading allskycam iamge")
        $("#allskycam").attr("src", "./images/hex-loader2.gif")
        setTimeout(function () {
            console.log("loading allskycam iamg inner ")
            d = new Date();
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

    function LightCurve(binning) {
        console.log("loading LC");

        $.getJSON('/lightcurve?hours=20', function (lightcurve) {
            if (lightcurve) {
                excessPlot(lightcurve, 5);
                //$('#lightcurve').html(excess);
            }
        });

        function refresh(){
            console.log("loading LC");
            $.getJSON('/lightcurve', function (lightcurve) {
                if (lightcurve) {
                    //$('#lightcurve').html(excess);
                }
            });
        }

        function excessPlot(lightcurve, binning){

            var formatter = d3.time.format("%Y-%m-%dT%H:%M:%S.%L");

            var data = _.map(lightcurve, function(v){
                var range = v[0];
                var value = v[1];
                var date  = formatter.parse(range.start);
                var alpha = 1.0 / value.numberOfOffRegions;
                var excess =  value.signalEvents - value.backgroundEvents * alpha;
                var lower = excess - Math.sqrt(value.signalEvents + value.backgroundEvents * alpha)*0.5;
                var upper = excess + Math.sqrt(value.signalEvents + value.backgroundEvents * alpha)*0.5;
                return {"date":date,
                    "excess":excess,
                    "lower": lower,
                    "upper":upper,
                    "signal":value.signalEvents,
                    "background": value.backgroundEvents
                };
            });
            var dates = _.map(data,'date');

            var bars = d3_time.timeSecond.count(d3.min(dates), d3.max(dates));

            var margin = {top: 40, right: 40, bottom: 40, left:40},
                width = 700,
                height = 400;

            var domainWidth = width - margin.left - margin.right;

            var barWidth = domainWidth/(bars + 1);
            var barHeight = 2;

            var errorBarHeight = 0.5;
            var errorBarWidth = barWidth*0.66;

            var earliestDate = d3.min(dates);
            var latestDate = d3.max(dates);

            var maxExcess = d3.max(data, function(d) { return d.excess; });
            var minExcess = d3.min(data, function(d) { return d.excess; });
//console.log(_.map(data,'lower'))

            var x = d3.time.scale()
                .domain([earliestDate, d3.time.second.offset(latestDate, 1)])
                .rangeRound([0, width - margin.left - margin.right]);

            var y = d3.scale.linear()
                .domain([minExcess - 1, maxExcess  + 1])
                .range([height - margin.top - margin.bottom, 0]);

            var xAxis = d3.svg.axis()
                .scale(x)
                .tickValues(dates)
                .tickFormat(d3.time.format('%H:%M:%S'))
                .tickSize(6)
                .tickPadding(5);

            var tooltip = d3.select("#lightcurve").append("div")
                .attr("class", "tooltip")
                .style("opacity", 0.0);

            var yAxis = d3.svg.axis()
                .scale(y)
                .orient('left')
                .tickPadding(8);

            var svg = d3.select('#lightcurve').append('svg')
                .attr('class', 'chart')
                .attr('width', width)
                .attr('height', height)
                .append('g')
                .attr('transform', 'translate(' + margin.left + ', ' + margin.top + ')');



// make gray backgorund rectangle
            svg.append("rect")
                .style("fill", "#f8f8f8")
                .attr("x", x(earliestDate))
                .attr("y", y(maxExcess + 2))
                .attr("width", domainWidth)
                .attr("height", y(minExcess) - y(maxExcess + 3))

            var selectedData = svg.selectAll('.chart')
                .data(data).enter();

            selectedData.append('rect')
                .attr('class', 'bar')
                .attr('x', function(d) { return x(d.date); })
                .attr('y', function(d) { return y(d.excess) - 0.5* barHeight })
                .attr('width', domainWidth/(bars + 1))
                .attr('height', barHeight);

//add invisible rectangle for tooltip hover
            selectedData.append('rect')
                .style("fill", "#ffffff")
                .style("opacity", "0")
                .attr('x', function(d) { return x(d.date); })
                .attr('y', function(d) { return y(d.excess) - 0.5* 50 })
                .attr('width', domainWidth/(bars + 1))
                .attr('height', function(d) {
                    return 50;
                })
                .on("mouseover", function(d) {

                    var x = d3.mouse(this)[0];
                    var y = d3.mouse(this)[1];
                    tooltip.transition()
                        .duration(300)
                        .style("opacity", .9);
                    tooltip.html( "<h1>" + "Excess: " + numeral(d.excess).format('0.00') + "</h1>"
                            + "Signal: "+ (d.signal) + "<br/>"
                            + "Background: " + (d.background))
                        .style("left", (x + 50) + "px")
                        .style("top", (y) + "px");
                })
                .on("mouseout", function(d) {
                    tooltip.transition()
                        .duration(500)
                        .style("opacity", .0);
                });
    //errorbars
            selectedData.append('rect')
                .attr('class', 'error')
                .attr('x', function(d) { return x(d.date) + (barWidth - errorBarWidth)/2; })
                .attr('y', function(d) { return y(d.lower) - 0.5* barHeight })
                .attr('width', errorBarWidth)
                .attr('height', errorBarHeight);

            selectedData.append('rect')
                .attr('class', 'error')
                .attr('x', function(d) { return x(d.date) + (barWidth - errorBarWidth)/2; })
                .attr('y', function(d) { return y(d.upper) - 0.5* barHeight })
                .attr('width', errorBarWidth)
                .attr('height', errorBarHeight);

            selectedData.append('line')
                .attr('class', 'error')
                .attr('x1', function(d) { return x(d.date) + barWidth/2; })
                .attr('y1', function(d) { return y(d.upper) - 0.5* barHeight })
                .attr('x2', function(d) { return x(d.date) + barWidth/2; })
                .attr('y2', function(d) { return y(d.lower) - 0.5* barHeight });

            svg.append('g')
                .attr('class', 'x axis')
                .attr('transform', 'translate(0, ' + (height - margin.top - margin.bottom) + ')')
                .call(xAxis)
                .selectAll("text")
                .style("text-anchor", "end")
                .attr("dx", "-.8em")
                .attr("dy", ".15em")
                .attr("transform", function(d) {
                    return "rotate(-40)"
                });

            svg.append("line")          // attach a line
                .style("stroke", "lightgray")  // colour the line
                .style("stroke-width", "0.5px")
                .attr("x1", x(earliestDate))     // x position of the first end of the line
                .attr("y1", y(0))      // y position of the first end of the line
                .attr("x2", x(latestDate) + domainWidth/(bars + 1))     // x position of the second end of the line
                .attr("y2", y(0));    // y position of the second end of the line

            svg.append('g')
                .attr('class', 'y axis')
                .call(yAxis);
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
                    height: 290,
                    width: 450
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
                    height: 290,
                    width: 450
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