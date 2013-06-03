### define
hammer : Hammer
./behavior : Behavior
###

class DragBehavior extends Behavior

  activate : ->

    @hammerContext = Hammer( $("#process-graph")[0] )
      .on("drag", ".node", @dragMoveNode)
      .on("drag", ".cluster", @dragMoveCluster)
      .on("dragstart", @dragStart)


  deactivate : ->

    @hammerContext
      .off("drag", @dragMoveNode)
      .off("drag", @dragMoveCluster)
      .off("dragstart", @dragStart)


  dragStart : (event) =>

    @offset = $("#process-graph").offset()
    @scaleValue = $(".zoom-slider input").val()

    @startPoint = @mousePosition(event)

  dragMoveNode : (event) =>

    node = d3.select(event.gesture.target).datum()
    mouse = @mousePosition(event)

    delta =
      x : mouse.x - @startPoint.x
      y : mouse.y - @startPoint.y

    @graph.moveNode(node, delta, true)

    @startPoint = mouse

  dragMoveCluster : (event) =>

    clusterId = $(event.gesture.target).data("id")
    mouse = @mousePosition(event)

    delta =
      x : mouse.x - @startPoint.x
      y : mouse.y - @startPoint.y

    @graph.moveCluster(clusterId, delta)

    @startPoint = mouse


