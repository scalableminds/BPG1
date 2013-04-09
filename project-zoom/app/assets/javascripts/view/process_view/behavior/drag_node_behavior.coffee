### define
###

class DragNodeBehavior

  constructor : (@svg, @graph) ->


  activate : ->

    context = this
    @dragMoveClosure = (node) ->

      if context.dragging
        context.dragMove(node, this)

    @svg
      .delegate("mousedown", "circle", @dragStart)
      .delegate("mousemove", "circle", @dragMoveClosure)
      .on("mouseup", @dragEnd)


  deactivate : ->

    # yes, D3 has a weird syntax using "on" to enable/disable event handlers
    @svg
      .on("mousedown", null)
      .on("mouseup", null)
      .on("mousemove", null)


  dragStart : (node) =>

    unless @dragging
      @dragging = true


  dragEnd : =>

    if @dragging
      @dragging = false


  dragMove : (node, svgContext) ->

    node.x = d3.mouse(svgContext)[0]
    node.y = d3.mouse(svgContext)[1]

    d3.select(svgContext)
      .attr("cx", node.x)
      .attr("cy", node.y)

    # update edges when node are dragged around
    edges = d3.selectAll(".edge")
    edges.attr("d", (data) ->
      if data
        data.getLineSegment())

