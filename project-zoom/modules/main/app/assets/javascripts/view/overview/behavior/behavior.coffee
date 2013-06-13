### define
lib/event_mixin : EventMixin
app : app
###

class Behavior

  constructor : (@graph) ->

    EventMixin.extend(this)

    # @offset = @graph.$svgEl.offset()
    @scaleValue = if app.view.zoom.level is 0 then 1.0 else app.view.zoom.level


  mousePosition : (event, relativeToGraph = true) =>

    @scaleValue = if app.view.zoom.level is 0 then 1.0 else app.view.zoom.level

    unless relativeToGraph
      @scaleValue = 1.0
      @offset = { left: 0, top: 0}

    x = event.gesture.touches[0].pageX #- @offset.left
    y = event.gesture.touches[0].pageY #- @offset.top

    x /= @scaleValue
    y /= @scaleValue

    return { x: x, y: y }


  activate : ->

    return true

  deactivate : ->

    return false




