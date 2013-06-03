### define
./behavior : Behavior
hammer : Hammer
###

class PanBehavior extends Behavior

  constructor : (@graph) ->

    @active = false

  activate : ->

    unless @active
      @hammerContext = Hammer($("#process-graph")[0])
      .on("dragstart", @panStart)
      .on("drag", @pan)

      @active = true


  deactivate : ->

    if @active
      @hammerContext
        .off("dragstart", @panStart)
        .off("drag", @pan)

      @active = false


  panStart : (event) =>

    @offset = $("#process-graph").offset()
    @scaleValue = $(".zoom-slider input").val()

    @startPoint = @mousePosition(event)

    graphContainer = @graph.graphContainer


  pan : (event) =>

    target = d3.select(event.gesture.target)

    if target.classed("node") or target.classed("cluster")
      return

    mouse = @mousePosition(event)
    graphContainer = @graph.graphContainer
    transformation = d3.transform(graphContainer.attr("transform"))

    deltaX = ( mouse.x - @startPoint.x ) * @scaleValue
    deltaY = ( mouse.y - @startPoint.y ) * @scaleValue

    x = transformation.translate[0] + deltaX
    y = transformation.translate[1] + deltaY


    transformation.translate = [x, y]

    graphContainer.attr("transform", transformation.toString())

    @startPoint = mouse
