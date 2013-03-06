define [
    "graph"
  ],
  ( Graph ) ->

    class Controller

      WIDTH  = 960
      HEIGHT = 500

      constructor : ->

        @svg = d3.select("body")
          .append("svg")
          .attr("WIDTH", WIDTH)
          .attr("HEIGHT", HEIGHT)
          .attr("pointer-events", "all")

        #need for touch/mouse events for zooming

        @svg.append('svg:g')
        @svg.append('svg:rect')
          .attr('width', WIDTH)
          .attr('height', HEIGHT)
          .attr('fill', 'white')
          .call(
            d3.behavior.zoom()
              .on("zoom", ( => @redraw()) )
          )
        @zoom = @svg.append('svg:g')


        @graph = new Graph(@zoom, WIDTH, HEIGHT)

      redraw : ->
        @zoom.attr("transform","translate(#{d3.event.translate}) scale(#{d3.event.scale})")

