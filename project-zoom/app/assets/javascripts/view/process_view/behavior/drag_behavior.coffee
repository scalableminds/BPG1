### define
hammer : Hammer
###

class dragBehavior

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

    @startX = event.gesture.touches[0].pageX - @offset.left
    @startY = event.gesture.touches[0].pageY - @offset.top


  dragMoveNode : (event) =>

    nodeId = $(event.gesture.target).data("id")

    x = event.gesture.touches[0].pageX - @offset.left
    y = event.gesture.touches[0].pageY - @offset.top

    x /= @scaleValue
    y /= @scaleValue

    @moveNode(nodeId, x, y)



  dragMoveCluster : (event) =>

    x = event.gesture.touches[0].pageX - @offset.left
    y = event.gesture.touches[0].pageY - @offset.top

    x /= @scaleValue
    y /= @scaleValue

    svgPath = d3.select(event.gesture.target)
    cluster = svgPath.datum()

    distX = x - @startX
    distY = y - @startY

    #move waypoints
    for waypoint in cluster.waypoints
      waypoint.x += distX
      waypoint.y += distY

    #actually move them
    svgPath.attr("d", (data) -> data.getLineSegment())

    #move all child nodes

    @startX = x
    @startY = y

  moveNode : (nodeId, x, y) ->

    svgContainer = $("div[data-id=#{nodeId}]").closest("foreignObject")[0]
    halfWidth = d3.select(svgContainer).datum().getSize() / 2

    d3.select(svgContainer)
      .attr(
        x : (data) -> data.x = x - halfWidth
        y : (data) -> data.y = y - halfWidth
      )

    # update edges when node are dragged around
    edges = d3.selectAll(".edge")
    edges.attr("d", (data) ->
      if data
        data.getLineSegment())
