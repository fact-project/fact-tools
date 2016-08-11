const d3 = require('d3');
const _ = require('lodash');

function DataRatePlot(parentID, data, radius=3, duration=900){

    this.data = data;
    var margin = {
            top: 40,
            right: 40,
            bottom: 40,
            left: 40
        },
        width = 600,
        height = 400;

    var dates = _.map(data, 'date');
    var earliestDate = d3.min(dates);
    var latestDate = d3.max(dates);

    var x = d3.time.scale()
        .domain([earliestDate, latestDate])
        .rangeRound([0, width - margin.left - margin.right]);

    var y = d3.scale.linear()
        .domain([0, 100])
        .range([height - margin.top - margin.bottom, 0]);


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

    //gray background
    svg.append("rect")
        .style("fill", "#f8f8f8")
        .attr("x", x(earliestDate))
        .attr("y", y(100))
        .attr("width", x(latestDate) - x(earliestDate))
        .attr("height", y(0) - y(100));

    svg.append("text")
        .attr("text-anchor", "middle")
        .attr("class", "axis_label")
        .attr("transform", "translate("+ 15 +","+ y(50)+")rotate(-90)")
        .text("Events per Second");
    // draw axes

    // draw the x axis
    var xAxis = d3.svg.axis()
        .scale(x)
        .orient('bottom')
        .tickFormat(d3.time.format("%H:%M:%S"));

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

    svg.append('g')
        .attr('class', 'y axis')
        .call(yAxis);

    var circles = svg.selectAll('circle').data(data, function(d) {
        return d.date
    });

    circles.enter()
        .append("circle")
        .attr("r", radius)
        .attr('class', 'plot_dots')
        .attr("cx", function(d) {
            return x(d.date);
        })
        .attr("cy", function(d) {
            return y(d.rate);
        });

    // Update X Axis
    svg.select(".x.axis")
        .transition()
        .duration(duration)
        .call(xAxis)

    svg.select(".x.axis")
        .selectAll("text")
        .style("text-anchor", "end")
        .attr("dx", "-.8em")
        .attr("dy", ".15em")
        .attr("transform", function(d) {
            return "rotate(-40)"
        });

    //  console.log(rate_tuples.length)
    //  rate_tuples = _.concat(new_tuples, rate_tuples);
    //	rate_tuples = _.take(rate_tuples,30);
    //  console.log(rate_tuples.length)
    /*
     var dates = _.map(rate_tuples, 'date');
     var diff = x(a.date) - x(d3.max(dates));
     console.log(diff);
     console.log(a.date);
     console.log(d3.max(dates));
     rate_tuples.push(a);
     rate_tuples.shift();
     */

    this.update = function updateDataRatePlot(newData, limit=100){
        if (data.length > limit){
            data  = _.drop(data, newData.length);
        }
        data = _.concat(data, newData);

        var dates = _.map(data, 'date');
        var earliestDate = d3.min(dates);
        var latestDate = d3.max(dates);
        x.domain([earliestDate, latestDate]);

        svg.select(".x.axis")
            .transition()
            .duration(duration)
            .call(xAxis);

        svg.select(".x.axis")
            .selectAll("text")
            .style("text-anchor", "end")
            .attr("dx", "-.8em")
            .attr("dy", ".15em")
            .attr("transform", function(d) {
                return "rotate(-40)";
            });
        var circles = svg.selectAll('circle').data(data, function(d) {
            return d.date;
        });

        circles
            .transition()
            .duration(duration)
            .attr("cx", function(d) {
                return x(d.date);
            })
            .attr("cy", function(d) {
                return y(d.rate);
            });

        circles.enter()
            .append("circle")
            .attr('class', 'plot_dots')
            .attr("r", 0)
            .attr("cx", function(d) {
                return x(d.date);
            })
            .attr("cy", function(d) {
                return y(d.rate);
            })
            .transition()
            .duration(duration)
            .attr("r", radius);

        circles.exit()
            .attr("r", radius)
            .transition()
            .duration(duration)
            .attr("cx", function(d) {
                return x(d.date);
            })
            .attr("cy", function(d) {
                return y(d.rate);
            })
            .attr("r", 0)
            .remove();

    }
}

module.exports = DataRatePlot;
//
//var getRandomData = function(N) {
//    var data = [];
//    for (var i = 1; i < N + 1; i++) {
//        data.push({
//            "date": moment().subtract(i, 'seconds').toDate(),
//            "rate": Math.random()*10 + 80,
//        });
//    }
//    return _.reverse(data);
//}
//
//var asdf = getRandomData(100)
//console.log(asdf)
//
//this.p =  new DataRatePlot('#chart', asdf, radius=2);
//window.setInterval(function(){
//    var data = getRandomData(2);
//    this.p.update(data)
//}, 3000);
