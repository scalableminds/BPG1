### define
core_ext : CoreExt
hammer : Hammer
./behavior : Behavior
###

class ConnectBehavior extends Behavior

  constructor : ( @graph, @container ) ->

    # line that is displayed when dragging a new edge between nodes
    if $(".drag-line").length == 0
      @dragLine = @container.insert("svg:path",":first-child") #prepend for proper zOrdering
      @dragLine
        .attr("class", "hidden drag-line")
        .style('marker-end', 'url(#end-arrow)')
    else
      @dragLine = d3.select(".drag-line")


  activate : ->

    @hammerContext = Hammer( $("svg")[0] )
      .on("drag", ".node-image", @dragMove)
      .on("dragend", ".node-image", @dragEnd)
      .on("dragstart", ".node-image", @dragStart)


  deactivate : ->

    @hammerContext
      .off("drag", @dragMove)
      .off("dragend", @dragEnd)
      .off("dragstart", @dragStart)

    @dragLine.classed("hidden", true)


  dragStart : (event) =>

    @offset = $("svg").offset()
    @scaleValue = $(".zoom-slider input").val()


  dragEnd : (event) =>

    startID = $(event.gesture.startEvent.target).data("id")
    nodeID = $(event.target).data("id")

    unless startID == nodeID
      @graph.addEdge(startID, nodeID)

    @dragLine.classed("hidden", true)


  dragMove : (event) =>

    svgContainer = $(event.gesture.target).closest("foreignObject")[0]

    mouse = @mousePosition(event)

    nodeData = d3.select(svgContainer).datum()
    lineStartX = nodeData.x
    lineStartY = nodeData.y

    @dragLine
      .classed("hidden", false)
      .attr("d", "M #{lineStartX},#{lineStartY} L #{mouse.x},#{mouse.y}")
