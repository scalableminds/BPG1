### define
d3 : d3
./graph : Graph
core_ext : CoreExt
lib/event_mixin : EventMixin
###

class InteractiveGraph extends Graph

  constructor : (@container, @svg) ->

    EventMixin.extend(this)

    @mode = "connect"

    @initDragging() #call before super for z-Ordering
    super(@container)

    @initEventHandlers()


  drawNodes : ->

    super()
    #@circles.call(@drag)


  initEventHandlers : ->

    if @mode == "drag"
      #@drag.on "drag", @dragMove
    else if @mode == "connect"

      @svg
        #.hammer()
        .on( "mousemove", @mouseMove )
        .delegate( "mouseup.cancelDrag","rect", @mouseUp )

        #handler for the nodes
        .delegate( "mousedown.beginDrag", "circle", @nodeMouseDown )
        .delegate( "mouseup.endDrag", "circle", @nodeMouseUp )
        #.on( "tap", ".node", @nodeMouseDown )
        #.on( "dragend", ".node", @nodeMouseUp )


  initDragging : ->

    #@drag = d3.behavior.drag()
    #@drag.origin(Object)


    # line that is displayed when dragging a new edge between nodes
    @dragLine = @container.append("svg:path")
    @dragLine
      .attr("class", "edge hidden")
      .style('marker-end', 'url(#end-arrow)')


  nodeMouseDown : (node) =>

    unless @startDrag
      @startDrag = node

      console.log "donw"


  nodeMouseUp : (node) =>

    console.log "up"

    if @startDrag == node
      @startDrag = null

    else

      @addEdge(@startDrag.id, node.id)
      @startDrag = null
      @dragLine.classed("hidden", true)


  mouseUp : =>

    console.log "cancel"

    if @startDrag
      @startDrag = null
      @dragLine.classed("hidden", true)


  mouseMove : =>

    if @startDrag
      mouseContext = @container[0][0]

      @dragLine
        .classed("hidden", false)
        .attr("d", "M #{@startDrag.x},#{@startDrag.y} L #{d3.mouse(mouseContext)[0]},#{d3.mouse(mouseContext)[1]}")


  dragMove : (d) ->

    d3.select(this)
      .attr("cx", d.x = d3.event.x)
      .attr("cy", d.y = d3.event.y)

    # update edges when node are dragged around
    edges = d3.selectAll(".edge")
    edges.attr("d", (data) -> data.getLineSegment())








