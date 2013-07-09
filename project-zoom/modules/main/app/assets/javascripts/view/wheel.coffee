### define
jquery : $
underscore : _
app : app
hammer : Hammer
lib/event_mixin : EventMixin
###

BUFFER_THRESHOLD = 50

Wheel = (el) ->

  EventMixin.extend(this)

  isActivated = false

  buffer = 0

  mouseDown = false

  mouseDownHandler = -> mouseDown = true; return
  mouseUpHandler = -> mouseDown = false; return
  mouseWheelHandler = (event, delta, deltaX, deltaY) =>

    event.preventDefault()
    return if mouseDown

    buffer += deltaY
    unless -BUFFER_THRESHOLD < buffer < BUFFER_THRESHOLD
      
      if deltaY < 0 
        delta = Math.ceil(buffer / BUFFER_THRESHOLD)
      else
        delta = Math.floor(buffer / BUFFER_THRESHOLD)

      buffer = buffer % BUFFER_THRESHOLD

      @trigger("delta", delta, [ event.pageX, event.pageY ])


  pinchHandler = ( {gesture} ) =>

    scale = gesture.scale - 1
    @trigger("delta", scale / Math.abs(scale), [ gesture.center.pageX, gesture.center.pageY ])


  @activate = ->

    unless isActivated
      isActivated = true
      Hammer(document.body)
        .on("touch", mouseDownHandler)
        .on("release", mouseUpHandler)

      $(el).on("mousewheel", mouseWheelHandler)
      Hammer(el).on("pinch", pinchHandler)


  @deactivate = ->

    if isActivated
      isActivated = false
      Hammer(document.body)
        .off("touch", mouseDownHandler)
        .off("release", mouseUpHandler)

      $(el).off("mousewheel", mouseWheelHandler)
      Hammer(el).off("pinch", pinchHandler)


  return