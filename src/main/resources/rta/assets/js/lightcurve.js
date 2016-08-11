const d3 = require('d3');
const d3_time = require('d3-time');

function LightCurve(parentID, data, binning) {

    var dates = _.map(data, 'date');

    var bars = d3_time.timeSecond.count(d3.min(dates), d3.max(dates))/(binning*60);
    var margin = {
            top: 40,
            right: 40,
            bottom: 40,
            left: 40
        },
        width = 600,
        height = 400;


    var domainWidth = width - margin.left - margin.right;

    var barWidth = domainWidth / (bars + 1);
    var barHeight = 2;

    var errorBarHeight = 0.5;
    var errorBarWidth = barWidth * 0.66;

    var earliestDate = d3.min(dates);
    var latestDate = d3.max(dates);

    var maxExcess = d3.max(data, function(d) {
        return d.excess;
    });
    var minExcess = d3.min(data, function(d) {
        return d.excess;
    });
    //console.log(_.map(data,'lower'))

    var x = d3.time.scale()
        .domain([earliestDate, d3.time.minute.offset(latestDate, binning)])
        .rangeRound([0, width - margin.left - margin.right]);

    var y = d3.scale.linear()
        .domain([minExcess - 5, maxExcess + 3])
        .range([height - margin.top - margin.bottom, 0]);

    var xAxis = d3.svg.axis()
        .scale(x)
        .tickValues(dates)
        .tickFormat(d3.time.format('%H:%M:%S'))
        .tickSize(6)
        .tickPadding(5);

    var tooltip = d3.select(parentID).append("div")
        .attr("class", "tooltip")
        .style("opacity", 0.0);

    var yAxis = d3.svg.axis()
        .scale(y)
        .orient('left')
        .tickPadding(8);

    var svg = d3.select(parentID).append('svg')
        .attr('class', 'chart')
        .attr('width', width)
        .attr('height', height)
        .append('g')
        .attr('transform', 'translate(' + margin.left + ', ' + margin.top + ')');



    // make gray background rectangle
    svg.append("rect")
        .style("fill", "#f8f8f8")
        .attr("x", x(earliestDate))
        .attr("y", y(maxExcess + 4))
        .attr("width", domainWidth)
        .attr("height", y(minExcess - 5) + Math.abs(y(maxExcess + 4)));


    svg.append("text")
        .attr("text-anchor", "middle")
        .attr("class", "axis_label")
        .attr("transform", "translate("+ 15 +","+ y(maxExcess*0.5)+")rotate(-90)")
        .text("Excess Events per hour");

    var selectedData = svg.selectAll('.chart')
        .data(data).enter();

    selectedData.append('rect')
        .attr('class', 'bar')
        .attr('x', function(d) {
            return x(d.date);
        })
        .attr('y', function(d) {
            return y(d.excess) - 0.5 * barHeight
        })
        .attr('width', domainWidth / (bars + 1))
        .attr('height', barHeight);

    //add invisible rectangle for tooltip hover
    selectedData.append('rect')
        .style("fill", "#ffffff")
        .style("opacity", "0")
        .attr('x', function(d) {
            return x(d.date);
        })
        .attr('y', function(d) {
            return y(d.excess) - 0.5 * 50
        })
        .attr('width', domainWidth / (bars + 1))
        .attr('height', function(d) {
            return 50;
        })
        .on("mouseover", function(d) {

            var x = d3.mouse(this)[0];
            var y = d3.mouse(this)[1];
            tooltip.transition()
                .duration(300)
                .style("opacity", .9);
            tooltip.html("<h1>" + "Excess: " + numeral(d.excess).format('0.00') + "</h1>" + "Signal: " + (d.signal) + "<br/>" + "Background: " + (d.background))
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
        .attr('x', function(d) {
            return x(d.date) + (barWidth - errorBarWidth) / 2;
        })
        .attr('y', function(d) {
            return y(d.lower) - 0.5 * barHeight
        })
        .attr('width', errorBarWidth)
        .attr('height', errorBarHeight);

    selectedData.append('rect')
        .attr('class', 'error')
        .attr('x', function(d) {
            return x(d.date) + (barWidth - errorBarWidth) / 2;
        })
        .attr('y', function(d) {
            return y(d.upper) - 0.5 * barHeight
        })
        .attr('width', errorBarWidth)
        .attr('height', errorBarHeight);

    selectedData.append('line')
        .attr('class', 'error')
        .attr('x1', function(d) {
            return x(d.date) + barWidth / 2;
        })
        .attr('y1', function(d) {
            return y(d.upper) - 0.5 * barHeight
        })
        .attr('x2', function(d) {
            return x(d.date) + barWidth / 2;
        })
        .attr('y2', function(d) {
            return y(d.lower) - 0.5 * barHeight
        });

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

    svg.append("line") // attach a line
        .style("stroke", "lightgray") // colour the line
        .style("stroke-width", "0.5px")
        .attr("x1", x(earliestDate)) // x position of the first end of the line
        .attr("y1", y(0)) // y position of the first end of the line
        .attr("x2", x(latestDate) + domainWidth / (bars + 1)) // x position of the second end of the line
        .attr("y2", y(0)); // y position of the second end of the line

    svg.append('g')
        .attr('class', 'y axis')
        .call(yAxis);
}

module.exports = LightCurve;