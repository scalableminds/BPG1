### define
core_ext : CoreExt
hammer : Hammer
###

class ConnectNodesBehavior

  constructor : ( @graph, @container ) ->

    # line that is displayed when dragging a new edge between nodes
    if $(".dragLine").length == 0
      @dragLine = @container.insert("svg:path",":first-child") #prepend for proper zOrdering
      @dragLine
        .attr("class", "hidden dragLine")
        .style('marker-end', 'url(#end-arrow)')
    else
      @dragLine = d3.select(".dragLine")


  activate : ->

    @hammerContext = Hammer( $("svg")[0] )
      .on("drag", ".node", @dragMove)
      .on("dragend", ".node", @dragEnd)


  deactivate : ->

    @hammerContext
      .off("drag", @dragMove)
      .off("dragend", @dragEnd)

    @dragLine.classed("hidden", true)


  dragEnd : (event) =>

    # checking localName is a bit of a hack
    startEvent = event.gesture.startEvent
    if event.target == startEvent.target or not (event.target.localName == "circle")
      @dragLine.classed("hidden", true)
      return

    else

      nodeID = event.target.__data__.id
      startID = startEvent.target.__data__.id

      @graph.addEdge(startID, nodeID)
      @dragLine.classed("hidden", true)


  dragMove : (event) =>

    offset = $("svg").offset()
    scaleValue = $(".zoomSlider input").val()

    x = event.gesture.touches[0].pageX - offset.left
    y = event.gesture.touches[0].pageY - offset.top

    x /= scaleValue
    y /= scaleValue

    startEvent = event.gesture.startEvent
    lineStartX = d3.select(startEvent.target).attr("cx")
    lineStartY = d3.select(startEvent.target).attr("cy")

    #lineStartX = startEvent.touches[0].pageX - offset.left
    #lineStartY = startEvent.touches[0].pageY - offset.top

    @dragLine
      .classed("hidden", false)
      .attr("d", "M #{lineStartX},#{lineStartY} L #{x},#{y}")
