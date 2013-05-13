### define
hammer : Hammer
###

class DragNodeBehavior

  activate : ->

    @hammerContext = Hammer( $("svg")[0] )
      .on("drag", ".nodeElement", @dragMove)
      .on("dragstart", ".nodeElement", @dragStart)


  deactivate : ->

    @hammerContext
      .off("drag", @dragMove)
      .off("dragstart", ".nodeElement", @dragStart)


  dragStart : (event) =>

    @offset = $("svg").offset()
    @scaleValue = $(".zoomSlider input").val()


  dragMove : (event) =>

    svgContainer = $(event.gesture.target).closest("foreignObject")[0]

    x = event.gesture.touches[0].pageX - @offset.left
    y = event.gesture.touches[0].pageY - @offset.top

    x /= @scaleValue
    y /= @scaleValue

    halfWidth = d3.select(svgContainer).datum().getSize() / 2

    d3.select(svgContainer)
      .attr("x", (data) -> data.x = x - halfWidth)
      .attr("y", (data) -> data.y = y - halfWidth)

    # update edges when node are dragged around
    edges = d3.selectAll(".edge")
    edges.attr("d", (data) ->
      if data
        data.getLineSegment())


