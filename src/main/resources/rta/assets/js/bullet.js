(function() {
    const d3 = require('d3');
// Chart design based on the recommendations of Stephen Few. Implementation
// based on the work of Clint Ivy, Jamie Love, and Jason Davies.
// http://projects.instantcognition.com/protovis/bulletchart/
    d3.bullet = function() {
        var orient = "left",
            reverse = false,
            vertical = false,
            ranges = bulletRanges,
            markers = bulletMarkers,
            measures = bulletMeasures,
            width = 380,
            height = 30,
            xAxis = d3.svg.axis();

        // For each small multiple…
        function bullet(g) {
            g.each(function(d, i) {
                var rangez = ranges.call(this, d, i).slice().sort(d3.descending),
                    markerz = markers.call(this, d, i).slice().sort(d3.descending),
                    measurez = measures.call(this, d, i).slice().sort(d3.descending),
                    g = d3.select(this),
                    extentX,
                    extentY;

                var wrap = g.select("g.wrap");
                if (wrap.empty()) wrap = g.append("g").attr("class", "wrap");

                if (vertical) {
                    extentX = height, extentY = width;
                    wrap.attr("transform", "rotate(90)translate(0," + -width + ")");
                } else {
                    extentX = width, extentY = height;
                    wrap.attr("transform", null);
                }

                // Compute the new x-scale.
                var x1 = d3.scale.linear()
                    .domain([0, Math.max(rangez[0], markerz[0], measurez[0])])
                    .range(reverse ? [extentX, 0] : [0, extentX]);

                // Retrieve the old x-scale, if this is an update.
                var x0 = this.__chart__ || d3.scale.linear()
                        .domain([0, Infinity])
                        .range(x1.range());

                // Stash the new scale.
                this.__chart__ = x1;

                // Derive width-scales from the x-scales.
                var w0 = bulletWidth(x0),
                    w1 = bulletWidth(x1);

                // Update the range rects.
                var range = wrap.selectAll("rect.range")
                    .data(rangez);

                range.enter().append("rect")
                    .attr("class", function(d, i) { return "range s" + i; })
                    .attr("width", w0)
                    .attr("height", extentY)
                    .attr("x", reverse ? x0 : 0)

                d3.transition(range)
                    .attr("x", reverse ? x1 : 0)
                    .attr("width", w1)
                    .attr("height", extentY);

                // Update the measure rects.
                var measure = wrap.selectAll("rect.measure")
                    .data(measurez);

                measure.enter().append("rect")
                    .attr("class", function(d, i) { return "measure s" + i; })
                    .attr("width", w0)
                    .attr("height", extentY / 3)
                    .attr("x", reverse ? x0 : 0)
                    .attr("y", extentY / 3);

                d3.transition(measure)
                    .attr("width", w1)
                    .attr("height", extentY / 3)
                    .attr("x", reverse ? x1 : 0)
                    .attr("y", extentY / 3);

                // Update the marker lines.
                var marker = wrap.selectAll("line.marker")
                    .data(markerz);

                marker.enter().append("line")
                    .attr("class", "marker")
                    .attr("x1", x0)
                    .attr("x2", x0)
                    .attr("y1", extentY / 6)
                    .attr("y2", extentY * 5 / 6);

                d3.transition(marker)
                    .attr("x1", x1)
                    .attr("x2", x1)
                    .attr("y1", extentY / 6)
                    .attr("y2", extentY * 5 / 6);

                var axis = g.selectAll("g.axis").data([0]);
                axis.enter().append("g").attr("class", "axis");
                axis.attr("transform", vertical ? null : "translate(0," + extentY + ")")
                    .call(xAxis.scale(x1));
            });
            d3.timer.flush();
        }

        // left, right, top, bottom
        bullet.orient = function(_) {
            if (!arguments.length) return orient;
            orient = _ + "";
            reverse = orient == "right" || orient == "bottom";
            xAxis.orient((vertical = orient == "top" || orient == "bottom") ? "left" : "bottom");
            return bullet;
        };

        // ranges (bad, satisfactory, good)
        bullet.ranges = function(_) {
            if (!arguments.length) return ranges;
            ranges = _;
            return bullet;
        };

        // markers (previous, goal)
        bullet.markers = function(_) {
            if (!arguments.length) return markers;
            markers = _;
            return bullet;
        };

        // measures (actual, forecast)
        bullet.measures = function(_) {
            if (!arguments.length) return measures;
            measures = _;
            return bullet;
        };

        bullet.width = function(_) {
            if (!arguments.length) return width;
            width = +_;
            return bullet;
        };

        bullet.height = function(_) {
            if (!arguments.length) return height;
            height = +_;
            return bullet;
        };

        return d3.rebind(bullet, xAxis, "tickFormat");
    };

    function bulletRanges(d) {
        return d.ranges;
    }

    function bulletMarkers(d) {
        return d.markers;
    }

    function bulletMeasures(d) {
        return d.measures;
    }

    function bulletWidth(x) {
        var x0 = x(0);
        return function(d) {
            return Math.abs(x(d) - x0);
        };
    }

})();

function BulletChart(parentID, data) {
    data = prepareData(data);


    var margin = {
            top: 5,
            right: 40,
            bottom: 20,
            left: 120
        },
        width = 420 - margin.left - margin.right,
        height = 50 - margin.top - margin.bottom;

    var chart = d3.bullet()
        .width(width)
        .height(height);

    var svg = d3.select(parentID).selectAll("svg")
        .data(data)
        .enter().append("svg")
        .attr("class", "bullet")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
        .call(chart);

    var title = svg.append("g")
        .style("text-anchor", "end")
        .attr("transform", "translate(-6," + height / 2 + ")");

    title.append("text")
        .attr("class", "title")
        .text(function(d) {
            return d.title;
        });

    title.append("text")
        .attr("class", "subtitle")
        .attr("dy", "1em")
        .text(function(d) {
            return d.subtitle;
        });


    this.update = function updateBullets(newData){
        var r = prepareData(newData);
        svg.data(r).call(chart.duration(1000)); // TODO automatic transition
    };

    function prepareData(data){
        return [{
            "title": "Memory",
            "subtitle": "RAM in GigaByte",
            "ranges": [128],
            "measures": [data.memory],
            "markers": [15]
        }, {
            "title": "CPU",
            "subtitle": "CPU usage in percent",
            "ranges": [100],
            "measures": [data.cpu],
            "markers": [90]
        }, {
            "title": "Storage",
            "subtitle": "Available storage in GigaByte",
            "ranges": [500],
            "measures": [data.storage],
            "markers": [200]
        } ];
    }
}
