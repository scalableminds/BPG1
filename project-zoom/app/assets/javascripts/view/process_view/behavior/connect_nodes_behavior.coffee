### define
core_ext : CoreExt
underscore : _
###

class ConnectNodesBehavior

  constructor : (@svg, @graph, @container) ->

    # line that is displayed when dragging a new edge between nodes
    if $(".dragLine").length == 0
      @dragLine = @container.insert("svg:path",":first-child") #prepend for proper zOrdering
      @dragLine
        .attr("class", "edge hidden dragLine")
        .style('marker-end', 'url(#end-arrow)')
    else
      @dragLine = d3.select(".dragLine")


  activate : ->

    @svg
      .on( "mousemove", @mouseMove )
      .delegate( "mouseup.cancelDrag","rect", @mouseUp )

      #handler for the nodes
      .delegate( "mousedown.beginDrag", "circle", @nodeMouseDown )
      .delegate( "mouseup.endDrag", "circle", @nodeMouseUp )


  deactivate : ->

    # yes, D3 has a weird syntax using "on" to enable/disable event handlers
    @svg
      .on( "mousemove", null )
      .on( "mouseup.cancelDrag", null )
      .on( "mousedown.beginDrag", null )
      .on( "mouseup.endDrag", null )

    @dragLine.classed("hidden", true)


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
