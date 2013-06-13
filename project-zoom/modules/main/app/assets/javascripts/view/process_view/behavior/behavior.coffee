### define
lib/event_mixin : EventMixin
app : app
###

class Behavior

  constructor : (@graph) ->

    EventMixin.extend(this)

    @offset = @graph.$svgEl.offset()

    @svgRoot = $("#process-graph")[0]
    @transformationGroup = @graph.graphContainer[0][0]


  mousePosition : (event, relativeToGraph = true) =>

    @scaleValue = app.view.process.zoom

    unless relativeToGraph
      @scaleValue = 1.0
      @offset = { left: 0, top: 0}

    x = event.gesture.touches[0].pageX - @offset.left
    y = event.gesture.touches[0].pageY - @offset.top

    x /= @scaleValue
    y /= @scaleValue

    return { x: x, y : y }


  transformPointToLocal : (point) ->

    p = @svgRoot.createSVGPoint()
    p.x = point.x
    p.y = point.y

    p.matrixTransform(@transformationGroup.getCTM.inverse())


  mouseToSVGLocalCoordinates : (event) ->

    p = @svgRoot.createSVGPoint()
    p.x = event.gesture.touches[0].pageX - @offset.left
    p.y = event.gesture.touches[0].pageY - @offset.top

    p.matrixTransform(@transformationGroup.getCTM().inverse())


  setCTM : (matrix) ->
    matrixString = "#{matrix.a} #{matrix.b} #{matrix.c} #{matrix.d} #{matrix.e} #{matrix.f}"
    @graph.graphContainer.attr("transform", "matrix(#{matrixString})")

  activate : ->

    return true

  deactivate : ->

    return false