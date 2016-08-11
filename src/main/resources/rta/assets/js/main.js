/*
	Prism by TEMPLATED
	templated.co @templatedco
	Released for free under the Creative Commons Attribution 3.0 license (templated.co/license)
*/

//
const $ = require('zeptojs');
const Hexmap = require('hexmap');
const numeral = require('numeral');
const _ = require('lodash');
const d3 = require('d3');
const BulletPlot = require('./bullet.js');
const DataRatePlot = require('./datarate.js');
const LightCurve = require('./lightcurve.js');


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
        console.log("loading allskycam iamge");
        $("#allskycam").attr("src", "./images/hex-loader2.gif");
        setTimeout(function () {
            console.log("loading allskycam img inner ");
            d = new Date();
            $("#allskycam").attr("src", "http://www.gtc.iac.es/multimedia/netcam/camaraAllSky.jpg?" + d.getTime());
        }, 4000);
    }


    function loadEvent() {
        console.log("loading Event");

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



    var binning = 20;

    var data = _.map(_.range(10), function(v) {
        var value = {"signalEvents":Math.floor(Math.random()*15),"backgroundEvents":Math.floor(Math.random()*10),};
        var date = d3.time.minute.offset(new Date(), -(v*binning + Math.random()));
        var alpha = 1.0 / 5.0;
        var excess = value.signalEvents - value.backgroundEvents * alpha;
        var lower = excess - Math.sqrt(value.signalEvents + value.backgroundEvents * alpha) * 0.5;
        var upper = excess + Math.sqrt(value.signalEvents + value.backgroundEvents * alpha) * 0.5;
        return {
            "date":date,
            "excess": excess,
            "lower": lower,
            "upper": upper,
            "signal": value.signalEvents,
            "background": value.backgroundEvents
        };
    });

    var l = new LightCurve('#lightcurve', data, binning);


    var p;
    var latestTimeStamp;
    var formatter = d3.time.format("%Y-%m-%dT%H:%M:%S.%L");

    $.getJSON('/datarate', function (rates) {
        if (rates != null ) {
            rates = _.map(rates, function(a){
                a.date = formatter.parse(a.date);
                return a;
            });
            latestTimeStamp = _.maxBy(rates, 'date').date;
            console.log(latestTimeStamp);
            p = new DataRatePlot('#datarate_chart', rates, radius=2);
            //$('#lightcurve').html(excess);
        }
    });




    window.setInterval(function(){
        if (p){
            $.getJSON('/datarate?timestamp='+formatter(latestTimeStamp), function (rates) {
                if (rates != null ) {

                    rates = _.map(rates, function(a){
                        a.date = formatter.parse(a.date);
                        return a;
                    });
                    latestTimeStamp = _.maxBy(rates, 'date').date;
                    console.log(latestTimeStamp);
                    p.update(rates);
                    //$('#lightcurve').html(excess);
                }
            });
        }
    }, 4*SECONDS);
    //


    loadSkyCamImage();
    window.setInterval(loadSkyCamImage, 5*MINUTES);
    //
    loadEvent();
    window.setInterval(loadEvent, 15*SECONDS);
    //
    //MemoryChart();
    //window.setInterval(MemoryChart.load, 30*SECONDS);
}

