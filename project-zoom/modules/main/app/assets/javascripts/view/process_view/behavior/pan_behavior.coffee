### define
./behavior : Behavior
hammer : Hammer
###

class PanBehavior extends Behavior

  activate : ->

    @hammerContext = Hammer($("#process-graph")[0])
    .on("dragstart", @panStart)
    .on("drag", @pan)


  deactivate : ->

    @hammerContext
      .off("dragstart")
      .off("dragmove")


  panStart : (event) =>

    @offset = $("#process-graph").offset()
    @scaleValue = $(".zoom-slider input").val()

    @startPoint = @mousePosition(event)

    graphContainer = @graph.graphContainer

    transformation = d3.transform(graphContainer.attr("transform"))
    @startPoint.x -= transformation.translate[0]
    @startPoint.y -= transformation.translate[1]


  pan : (event) =>

    target = d3.select(event.gesture.target)

    if target.classed("node") or target.classed("cluster")
      return

    mouse = @mousePosition(event)

    distX = mouse.x - @startPoint.x
    distY = mouse.y - @startPoint.y

    x = distX / @scaleValue
    y = distY / @scaleValue

    graphContainer = @graph.graphContainer

    transformation = d3.transform(graphContainer.attr("transform"))
    transformation.translate = [x, y]

    graphContainer.attr("transform", transformation.toString())

