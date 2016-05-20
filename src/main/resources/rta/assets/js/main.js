/*
	Prism by TEMPLATED
	templated.co @templatedco
	Released for free under the Creative Commons Attribution 3.0 license (templated.co/license)
*/

//
const $ = require('zeptojs');
const Hexmap = require('hexmap');


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



$(document).ready(init);


function init() {
    console.log("blabla")

    //initialize the hex display
    var parentID = 'fact_map';
    var size = 450;
    var radius = 5;
    this.camera = new Hexmap(parentID, size, radius);

    function loadHipsterContent() {
        console.log("loading hipster")
        $.getJSON('http://hipsterjesus.com/api/?paras=1&type=hipster-centric', function(data) {
            $('#hipstercontent').html( data.text );
        });
    }


    function loadEvent(){
        console.log("loading Event")

        $.getJSON('/event', function(event){
            if (event) {
                console.log(event)
                this.camera.update(event.image, duration = 1);
                $('#source').html( event.sourceName );
                $('#timestamp').html( event.timestamp );
            }
        });
    }


    function loadStatus(){
        console.log("loading Event")

        $.getJSON('/status', function(status){
            if (status) {
                console.log(status)
                $('#space').html( status.freeSpace );
                $('#memory').html( status.usedMemory );
                $('#cpus').html( status.availableProcessors);
            }
        });
    }


    loadHipsterContent();
    window.setInterval(loadHipsterContent, 60*SECONDS);

    loadEvent();
    window.setInterval(loadEvent, 10*SECONDS);

    loadStatus();
    window.setInterval(loadStatus, 30*SECONDS);

}