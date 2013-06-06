### define
hammer : Hammer
./behavior : Behavior
app : app
###

class DragBehavior extends Behavior

  activate : (@element) ->

    @hammerContext = Hammer( @element)
      .on("drag", ".node", @dragMoveNode)
      .on("drag", ".cluster", @dragMoveCluster)

    @startPoint = d3.select(@element).datum().get("position").toObject() #not pretty but works


  deactivate : ->

    @hammerContext
      .off("drag", @dragMoveNode)
      .off("drag", @dragMoveCluster)


  dragMoveNode : (event) =>

    node = d3.select(event.gesture.target).datum()
    mouse = @mousePosition(event)

    delta =
      x : mouse.x - @startPoint.x
      y : mouse.y - @startPoint.y

    @graph.moveNode(node, delta, true)

    @startPoint = mouse
    app.trigger "behavior:drag"


  dragMoveCluster : (event) =>

    clusterId = $(event.gesture.target).data("id")
    mouse = @mousePosition(event)

    delta =
      x : mouse.x - @startPoint.x
      y : mouse.y - @startPoint.y

    @graph.moveCluster(clusterId, delta)

    @startPoint = mouse


