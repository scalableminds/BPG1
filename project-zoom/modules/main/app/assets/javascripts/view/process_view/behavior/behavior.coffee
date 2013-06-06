### define
###

class Behavior

  constructor : (@graph) ->

    @offset = @graph.$svgEl.offset()

  mousePosition : (event, relativeToGraph = true) =>

    @scaleValue = app.view.zoom.level

    unless relativeToGraph
      @scaleValue = 1.0
      @offset = { left: 0, top: 0}

    x = event.gesture.touches[0].pageX - @offset.left
    y = event.gesture.touches[0].pageY - @offset.top

    x /= @scaleValue
    y /= @scaleValue

    return { x: x, y : y }


  activate : ->

    return true

  deactivate : ->

    return false