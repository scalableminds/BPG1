### define
hammer : Hammer
###

class DragNodeBehavior

  activate : ->

    @hammerContext = Hammer( $("svg")[0] )
      .on("drag", ".node", @dragMove)


  deactivate : ->

    @hammerContext
      .off("drag", @dragMove)


  dragMove : (event) ->

    offset = $("svg").offset()

    x = event.gesture.touches[0].pageX - offset.left
    y = event.gesture.touches[0].pageY - offset.top

    d3.select(this)
      .attr("cx", (data) -> data.x = x)
      .attr("cy", (data) -> data.y = y)

    # update edges when node are dragged around
    edges = d3.selectAll(".edge")
    edges.attr("d", (data) ->
      if data
        data.getLineSegment())


