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
    @scaleValue = $(".zoom-slider input").val()

    @startX = @mousePosition(event).x
    @startY = @mousePosition(event).y


  dragMoveNode : (event) =>

    nodeId = $(event.gesture.target).data("id")
    mouse = @mousePosition(event)

    @graph.moveNode(nodeId, mouse, true)


  dragMoveCluster : (event) =>

    clusterId = $(event.gesture.target).data("id")
    mouse = @mousePosition(event)

    distance =
      x : mouse.x - @startX
      y : mouse.y - @startY

    @graph.moveCluster(clusterId, distance)

    @startX = mouse.x
    @startY = mouse.y


