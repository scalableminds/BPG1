### define
lib/event_mixin : EventMixin
app : app
###

class Behavior

  constructor : (@graph) ->

    EventMixin.extend(this)

    @offset = @graph.$svgEl.offset()


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

    groupElement = @graph.graphContainer[0][0]
    transformationMatrix = groupElement.getCTM()

    p = $("svg")[0].createSVGPoint()
    p.x = point.x
    p.y = point.y

    p.matrixTransform(transformationMatrix.inverse())


  activate : ->

    return true

  deactivate : ->

    return false