### define
hammer : Hammer
./behavior : Behavior
../helpers/cluster : Cluster
app : app
###

class DragBehavior extends Behavior

  activate : (@element) ->

    @hammerContext = Hammer( @element)
      .on("drag", ".node", @dragMoveNode)
      .on("drag", ".cluster", @dragMoveCluster)

    @startPoint = @element.position
    #app.trigger "behavior:disable_panning"

  deactivate : ->

    @hammerContext
      .off("drag", @dragMoveNode)
      .off("drag", @dragMoveCluster)


  dragStart : (event) =>

    @startPoint = @mouseToSVGLocalCoordinates(event)


  dragMoveNode : (event) =>

    node = d3.select(@element).datum()
    mouse = @mouseToSVGLocalCoordinates(event)

    @graph.moveNode(node, mouse, true)

    @startPoint = mouse
    app.trigger "behavior:drag"


  dragMoveCluster : (event) =>

    cluster = d3.select(@element).datum()
    mouse = @mouseToSVGLocalCoordinates(event)

    delta =
      x : mouse.x - @startPoint.x
      y : mouse.y - @startPoint.y

    @graph.moveCluster(cluster, delta)

    @startPoint = mouse
    app.trigger "behavior:drag"


