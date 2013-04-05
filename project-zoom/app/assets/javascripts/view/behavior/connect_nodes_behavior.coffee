### define
core_ext : CoreExt
underscore : _
###

class ConnectNodesBehavior

  constructor : (@container, @svg, @graph) ->

    # line that is displayed when dragging a new edge between nodes
    unless _.isEmpty( $("#dragLine") )
      @dragLine = @container.append("svg:path")
      @dragLine
        .attr("class", "edge hidden")
        .attr("class", "dragLine")
        .style('marker-end', 'url(#end-arrow)')


  active : ->

    @svg
      .on( "mousemove", @mouseMove )
      .delegate( "mouseup.cancelDrag","rect", @mouseUp )

      #handler for the nodes
      .delegate( "mousedown.beginDrag", "circle", @nodeMouseDown )
      .delegate( "mouseup.endDrag", "circle", @nodeMouseUp )


  deactive : ->

    @svg
      .off( "mousemove", @mouseMove )
      .off( "mouseup.cancelDrag", @mouseUp )
      .off( "mousedown.beginDrag", @nodeMouseDown )
      .off( "mouseup.endDrag", @nodeMouseUp )


  nodeMouseDown : (node) =>

    unless @startDrag
      @startDrag = node


  nodeMouseUp : (node) =>

    if @startDrag == node
      @startDrag = null

    else

      @graph.addEdge(@startDrag.id, node.id)
      @startDrag = null
      @dragLine.classed("hidden", true)


  mouseUp : =>

    if @startDrag
      @startDrag = null
      @dragLine.classed("hidden", true)


  mouseMove : =>

    if @startDrag
      mouseContext = @container[0][0]

      @dragLine
        .classed("hidden", false)
        .attr("d", "M #{@startDrag.x},#{@startDrag.y} L #{d3.mouse(mouseContext)[0]},#{d3.mouse(mouseContext)[1]}")
