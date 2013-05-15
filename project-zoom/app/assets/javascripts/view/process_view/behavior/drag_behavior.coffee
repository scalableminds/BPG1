### define
hammer : Hammer
./behavior : Behavior
###

class DragBehavior extends Behavior

  activate : ->

    @hammerContext = Hammer( $("svg")[0] )
      .on("drag", ".nodeElement", @dragMoveNode)
      .on("drag", ".cluster", @dragMoveCluster)
      .on("dragstart", @dragStart)


  deactivate : ->

    @hammerContext
      .off("drag", @dragMoveNode)
      .off("drag", @dragMoveCluster)
      .off("dragstart", @dragStart)


  dragStart : (event) =>

    @offset = $("svg").offset()
    @scaleValue = $(".zoomSlider input").val()

    @startX = @mousePosition(event).x
    @startY = @mousePosition(event).y


  dragMoveNode : (event) =>

    nodeId = $(event.gesture.target).data("id")
    mouse = @mousePosition(event)

    @moveNode(nodeId, mouse.x, mouse.y)


  dragMoveCluster : (event) =>

    mouse = @mousePosition(event)

    svgPath = d3.select(event.gesture.target)
    cluster = svgPath.datum()

    distX = mouse.x - @startX
    distY = mouse.y - @startY

    #move waypoints
    for waypoint in cluster.waypoints
      waypoint.x += distX
      waypoint.y += distY

    #actually move them
    svgPath.attr("d", (data) -> data.getLineSegment())

    #move child nodes
    for node in cluster.nodes
      @moveNode(node.id, node.x + distX, node.y + distY)

    #move all child nodes

    @startX = mouse.x
    @startY = mouse.y


  # x, y are absolute positions
  moveNode : (nodeId, x, y) ->

    svgContainer = $("div[data-id=#{nodeId}]").closest("foreignObject")[0]
    halfWidth = d3.select(svgContainer).datum().getSize() / 2

    d3.select(svgContainer)
      .attr(
        x : (data) -> data.x = x; x - halfWidth
        y : (data) -> data.y = y; y - halfWidth
      )

    # update edges when node are dragged around
    edges = d3.selectAll(".edge")
    edges.attr("d", (data) ->
      if data
        data.getLineSegment())


