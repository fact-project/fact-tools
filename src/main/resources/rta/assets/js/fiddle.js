var lightcurve =[[{"start":"2016-5-25T17:05:47.331","end":"2016-5-25T17:06:2.331"},{"signalEvents":0,"backgroundEvents":2,"numberOfOffRegions":5}],
    [{"start":"2016-5-25T17:05:46.217","end":"2016-5-25T17:05:47.331"},{"signalEvents":6,"backgroundEvents":1,"numberOfOffRegions":5}],
    [{"start":"2016-5-25T17:05:45.102","end":"2016-5-25T17:05:46.217"},{"signalEvents":1,"backgroundEvents":0,"numberOfOffRegions":5}],
    [{"start":"2016-5-25T17:05:43.987","end":"2016-5-25T17:05:45.102"},{"signalEvents":0,"backgroundEvents":0,"numberOfOffRegions":5}],
    [{"start":"2016-5-25T17:05:42.844","end":"2016-5-25T17:05:43.987"},{"signalEvents":3,"backgroundEvents":4,"numberOfOffRegions":5}],
    [{"start":"2016-5-25T17:05:41.729","end":"2016-5-25T17:05:42.844"},{"signalEvents":5,"backgroundEvents":4,"numberOfOffRegions":5}],
    [{"start":"2016-5-25T17:05:40.615","end":"2016-5-25T17:05:41.729"},{"signalEvents":4,"backgroundEvents":1,"numberOfOffRegions":5}],
//[{"start":"2016-5-25T17:05:39.501","end":"2016-5-25T17:05:40.615"},{"signalEvents":1,"backgroundEvents":3,"numberOfOffRegions":5}],
//[{"start":"2016-5-25T17:05:38.386","end":"2016-5-25T17:05:39.501"},{"signalEvents":0,"backgroundEvents":0,"numberOfOffRegions":5}],
    [{"start":"2016-5-25T17:05:37.224","end":"2016-5-25T17:05:38.386"},{"signalEvents":0,"backgroundEvents":1,"numberOfOffRegions":5}],
    [{"start":"2016-5-25T17:05:36.104","end":"2016-5-25T17:05:37.224"},{"signalEvents":0,"backgroundEvents":2,"numberOfOffRegions":5}],
//[{"start":"2016-5-25T17:05:34.984","end":"2016-5-25T17:05:36.104"},{"signalEvents":7,"backgroundEvents":4,"numberOfOffRegions":5}],
    [{"start":"2016-5-25T17:05:33.866","end":"2016-5-25T17:05:34.984"},{"signalEvents":8,"backgroundEvents":3,"numberOfOffRegions":5}],
    [{"start":"2016-5-25T17:05:32.749","end":"2016-5-25T17:05:33.866"},{"signalEvents":0,"backgroundEvents":1,"numberOfOffRegions":5}]];

var formatter = d3.time.format("%Y-%m-%dT%H:%M:%S.%L");

var data = _.map(lightcurve, function(v){
    var range = v[0];
    var value = v[1];
    var date  = formatter.parse(range.start)
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

var bars = d3_time.timeSecond.count(d3.min(dates), d3.max(dates))

var margin = {top: 40, right: 40, bottom: 40, left:40},
    width = 700,
    height = 400;

var domainWidth = width - margin.left - margin.right;
var earliestDate = d3.min(dates);
var latestDate = d3.max(dates);

var maxExcess = d3.max(data, function(d) { return d.excess; })
var minExcess = d3.min(data, function(d) { return d.excess; })
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

var div = d3.select("#lightcurve").append("div")
    .attr("class", "tooltip")
    .style("opacity", 0);

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

var barHeight = 2;
var errorBarHeight = 0.5;
var errorBarWidth = 15;

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
    .attr('height', function(d) {
        return barHeight;
    });

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
        div.transition()
            .duration(300)
            .style("opacity", .9);
        div.html( "<h1>" + "Excess: " + (d.excess) + "</h1><br/>"
                + "Signal: "+ (d.signal) + "<br/>"
                + "Background: " + (d.background))
            .style("left", (d3.event.pageX  + 8) + "px")
            .style("top", (d3.event.pageY - 28) + "px");
    })
    .on("mouseout", function(d) {
        div.transition()
            .duration(500)
            .style("opacity", 0);
    });

selectedData.append('rect')
    .attr('class', 'error')
    .attr('x', function(d) { return x(d.date) + errorBarWidth/2; })
    .attr('y', function(d) { return y(d.lower) - 0.5* barHeight })
    .attr('width', (domainWidth/(bars + 1))-errorBarWidth)
    .attr('height', errorBarHeight);

selectedData.append('rect')
    .attr('class', 'error')
    .attr('x', function(d) { return x(d.date) + errorBarWidth/2; })
    .attr('y', function(d) { return y(d.upper) - 0.5* barHeight })
    .attr('width', domainWidth/(bars + 1) - errorBarWidth)
    .attr('height', errorBarHeight);

selectedData.append('line')
    .attr('class', 'error')
    .attr('x1', function(d) { return x(d.date) + errorBarWidth; })
    .attr('y1', function(d) { return y(d.upper) - 0.5* barHeight })
    .attr('x2', function(d) { return x(d.date) + errorBarWidth; })
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


