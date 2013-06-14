### define
lib/event_mixin : EventMixin
app : app
###

class Behavior

  constructor : (@graph) ->

    EventMixin.extend(this)

    @svgRoot = @graph.svgEl
    @transformationGroup = @graph.graphContainer[0][0]


  transformPointToLocal : (point) ->

    p = @svgRoot.createSVGPoint()
    p.x = point.x
    p.y = point.y

    p.matrixTransform(@transformationGroup.getCTM().inverse())


  mouseToSVGLocalCoordinates : (event, matrix) ->

    offset = @graph.$svgEl.offset()
    p = @svgRoot.createSVGPoint()

    p.x = event.gesture.touches[0].pageX - offset.left
    p.y = event.gesture.touches[0].pageY - offset.top

    transformationMatrix = matrix ? @transformationGroup.getCTM().inverse()
    p.matrixTransform(transformationMatrix)


  setCTM : (matrix) ->

    matrixString = "#{matrix.a} #{matrix.b} #{matrix.c} #{matrix.d} #{matrix.e} #{matrix.f}"
    @graph.graphContainer.attr("transform", "matrix(#{matrixString})")


  activate : ->

    return true

  deactivate : ->

    return false