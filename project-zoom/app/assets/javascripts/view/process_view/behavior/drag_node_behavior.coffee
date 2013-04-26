### define
hammer : Hammer
###

class DragNodeBehavior

  activate : ->

    @hammerContext = Hammer( $("svg")[0] )
      .on("drag", ".nodeElement", @dragMove)


  deactivate : ->

    @hammerContext
      .off("drag", @dragMove)


  dragMove : (event) ->

    svgContainer = this.parentNode.parentNode

    $svg = $("svg")
    offset = $svg.offset()
    scaleValue = $(".zoomSlider input").val()

    x = event.gesture.touches[0].pageX - offset.left
    y = event.gesture.touches[0].pageY - offset.top

    x /= scaleValue
    y /= scaleValue


    halfWidth = 34

    d3.select(svgContainer)
      .attr("x", (data) -> data.x = x - halfWidth)
      .attr("y", (data) -> data.y = y - halfWidth)

    # update edges when node are dragged around
    edges = d3.selectAll(".edge")
    edges.attr("d", (data) ->
      if data
        data.getLineSegment())


