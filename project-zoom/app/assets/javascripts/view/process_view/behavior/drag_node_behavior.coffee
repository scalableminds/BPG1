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

    $svg = $("svg")
    offset = $svg.offset()
    scaleValue = $(".zoomSlider input").val()

    x = event.gesture.touches[0].pageX - offset.left
    y = event.gesture.touches[0].pageY - offset.top

    x /= scaleValue
    y /= scaleValue

    if 0 < x < $svg.width() and 0 < y < $svg.height()

      d3.select(this)
        .attr("cx", (data) -> data.x = x)
        .attr("cy", (data) -> data.y = y)

      # update edges when node are dragged around
      edges = d3.selectAll(".edge")
      edges.attr("d", (data) ->
        if data
          data.getLineSegment())


