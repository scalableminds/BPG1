### define
d3 : d3
./graph : Graph
###

class InteractiveGraph extends Graph

  constructor : (@container) ->

    super(@container)

    @drag = d3.behavior.drag()
    @drag.origin(Object)
    @drag.on "drag", @dragMove


  drawNodes : ->

    super()
    @circles.call(@drag)


  dragMove : (d) ->

    d3.select(@)
      .attr("cx", d.x = d3.event.x)
      .attr("cy", d.y = d3.event.y)

    # update edges when node are dragged around
    edges = d3.selectAll(".edge")
    edges.attr("d", (data) ->
      #if d == data.source or d == data.target
        data.getLineSegment()
    )
