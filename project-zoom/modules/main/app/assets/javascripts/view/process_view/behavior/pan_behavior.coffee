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

    transformation = d3.transform(graphContainer.attr("transform"))
    # @startPoint.x -= transformation.translate[0]
    # @startPoint.y -= transformation.translate[1]


  pan : (event) =>

    target = d3.select(event.gesture.target)

    if target.classed("node") or target.classed("cluster")
      return

    mouse = @mousePosition(event)
    graphContainer = @graph.graphContainer
    transformation = d3.transform(graphContainer.attr("transform"))

    distX = mouse.x - @startPoint.x
    distY = mouse.y - @startPoint.y

    x = distX / @scaleValue
    y = distY / @scaleValue


    x = transformation.translate[0] + x
    y = transformation.translate[1] + y


    transformation.translate = [x, y]

    graphContainer.attr("transform", transformation.toString())

