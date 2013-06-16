### define
core_ext : CoreExt
hammer : Hammer
./behavior : Behavior
app : app
###

class ConnectBehavior extends Behavior

  constructor : ( @graph ) ->

    # line that is displayed when dragging a new edge between nodes
    if @graph.$el.find(".drag-line").length == 0
      @dragLine = @graph.graphContainer.insert("svg:path",":first-child") #prepend for proper zOrdering
      @dragLine
        .attr("class", "hide drag-line")
        .style('marker-end', 'url(#end-arrow)')
    else
      @dragLine = @graph.d3Element.select(".drag-line")

    super(@graph)

  activate : (@element) ->

    @hammerContext = Hammer( @graph.svgEl )
      .on("dragend", ".node", @addEdge)
      .on("tap", ".node", @addEdge)

    @graph.$el.on "mousemove", @move

    app.trigger "behavior:disable_panning"


  deactivate : ->

    @hammerContext
      .off("dragend", @addEdge)
      .off("tap", @addEdge)

    @graph.$el.off("mousemove", @move)
    @dragLine.classed("hide", true)

    app.trigger "behavior:enable_panning"


  addEdge : (event) =>

    startNode = d3.select(@element).datum()

    if targetElement = d3.select(event.target)
      currentNode = targetElement.datum()

      unless startNode == currentNode or typeof currentNode == "undefined"
        @graph.addEdge(startNode, currentNode)

    @dragLine.classed("hide", true)
    app.trigger "behavior:done"


  move : (event) =>

    mouse =
      x : event.offsetX
      y : event.offsetY

    mouse = @transformPointToLocalCoordinates(mouse)

    nodeData = d3.select(@element).datum()
    lineStart = nodeData.get("position").toObject()

    @dragLine
      .classed("hide", false)
      .attr("d", "M #{lineStart.x},#{lineStart.y} L #{mouse.x},#{mouse.y}")
