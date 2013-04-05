### define
###

class DragNodeBehavior

  constructor : (@container, @svg, @graph) ->


  active : ->

    @svg
      .delegate("mousedown", "circle", @dragStart)
      .delegate("mouseup", "circle", @dragEnd)
      .delegate("mousemove", "circle", @dragMove)


  deactive : ->

    @svg
      .off("mousedown", @dragStart)
      .off("mouseup", @dragEnd)
      .off("mousemove", @dragMove)


  dragStart : ->

    unless @dragging
      @dragging = true


  dragEnd : ->

    if @dragging
      @dragging = false


  dragMove : (node) ->

    if @dragging

      #mouseContext = @container[0][0]

      d3.select(this)
        .attr("cx", node.x = d3.mouse(this)[0])
        .attr("cy", node.y = d3.mouse(this)[1])

      # update edges when node are dragged around
      edges = d3.selectAll(".edge")
      edges.attr("d", (data) -> data.getLineSegment())
