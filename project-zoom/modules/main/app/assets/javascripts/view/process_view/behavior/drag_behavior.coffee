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

    @offset = @graph.$svgEl.offset()
    @scaleValue = app.view.zoom.level

    @startX = @mousePosition(event).x
    @startY = @mousePosition(event).y


  dragMoveNode : (event) =>

    node = d3.select(event.gesture.target).datum()
    mouse = @mousePosition(event)

    @graph.moveNode(node, mouse, true)


  dragMoveCluster : (event) =>

    clusterId = $(event.gesture.target).data("id")
    mouse = @mousePosition(event)

    distance =
      x : mouse.x - @startX
      y : mouse.y - @startY

    @graph.moveCluster(clusterId, distance)

    @startX = mouse.x
    @startY = mouse.y


