### define
hammer : Hammer
./behavior : Behavior
app : app
###

class DragBehavior extends Behavior

  activate : ->

    @hammerContext = Hammer( $(".graph svg")[0] )
      .on("drag", ".node-image", @dragMoveNode)
      .on("drag", ".cluster", @dragMoveCluster)
      .on("dragstart", @dragStart)


  deactivate : ->

    @hammerContext
      .off("drag", @dragMoveNode)
      .off("drag", @dragMoveCluster)
      .off("dragstart", @dragStart)


  dragStart : (event) =>

    @offset = $("#process-view").offset()
    @scaleValue = app.view.zoom.level
    @startPoint = @mousePosition(event)


  dragMoveNode : (event) =>

    node = $(event.gesture.target).datum()
    mouse = @mousePosition(event)

    @graph.moveNode(node, mouse, true)


  dragMoveCluster : (event) =>

    clusterId = $(event.gesture.target).data("id")
    mouse = @mousePosition(event)

    distance =
      x : mouse.x - @startPoint.x
      y : mouse.y - @startPoint.y

    @graph.moveCluster(clusterId, distance)

    @startX = mouse.x
    @startY = mouse.y


