### define
hammer : Hammer
./behavior : Behavior
app : app
###

class DragBehavior extends Behavior

  activate : ->

    @hammerContext = Hammer( @graph.svgEl )
      .on("drag", ".node", @dragMoveNode)
      .on("drag", ".cluster", @dragMoveCluster)
      .on("dragstart", @dragStart)


  deactivate : ->

    @hammerContext
      .off("drag", @dragMoveNode)
      .off("drag", @dragMoveCluster)
      .off("dragstart", @dragStart)


  dragStart : (event) =>

    return unless event.gesture

    @offset = @graph.$svgEl.offset()
    @scaleValue = app.view.zoom.level

    @startPoint = @mousePosition(event)

  dragMoveNode : (event) =>

    return unless event.gesture

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


