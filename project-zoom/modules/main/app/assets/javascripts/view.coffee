### define
jquery : $
underscore : _
app : app
./view/zoom : Zoom
./view/process_view : ProcessView
lib/exec_queue : ExecQueue
###

app.addInitializer ->

  app.view = 
    zoom : new Zoom()


activeView = null
ensureProcessView = ->

  if not (activeView instanceof ProcessView)
    activeView = new ProcessView(app.model.project)
    $(".content").append(activeView.el)

  return

stateMachine =

  "[0,.6]->[.2,.5]" : (level, position) ->

    normalizedLevel = (level - .2) / .3

    ensureProcessView()
    activeView.$el.css(
      "transform" : "scale(#{normalizedLevel})"
      "transformOrigin" : if position then "#{position[0]}px #{position[1]}px" else ""
    )


  "[.2,.5]->[.5,3]" : (level) ->

    activeView.activate()
    $(document.body).off("mousewheel", mouseWheelHandler)


  "[.6,3]->[.2,.5]" : ->

    activeView.deactivate()
    $(document.body).on("mousewheel", mouseWheelHandler)


  "[.2,.5]->[0,.1]" : ->

    activeView?.$el.remove()
    activeView = null


execQueue = ExecQueue()


execStateMachine = (newValue, oldValue, rules, otherArgs...) ->

  _.forOwn(rules, (funcs, rule) ->

    [x, a0, b0, a1, b1] = rule.match(/\[([\d.]+),([\d.]+)\]->\[([\d.]+),([\d.]+)\]/)

    if +a0 <= oldValue <= +b0 and +a1 <= newValue <= +b1
      funcs = [ funcs ] unless _.isArray(funcs)
      funcs.forEach( (func) -> 
        execQueue -> func(newValue, otherArgs...)
      )

    return
  )


mouseWheelHandler = do ->

  mouseDown = false

  mouseDownHandler = -> mouseDown = true; return
  mouseUpHandler = -> mouseDown = false; return
  mouseWheelHandler = (event, delta, deltaX, deltaY) ->

    event.preventDefault()
    return if mouseDown
    if deltaY != 0
      app.view.zoom.changeZoom(deltaY / Math.abs(deltaY) * .1, [ event.pageX, event.pageY ])

  Hammer(document.body)
    .on("touch", mouseDownHandler)
    .on("release", mouseUpHandler)

  mouseWheelHandler


oldZoomLevel = 0

setActiveView = (zoomLevel, position) ->

  execStateMachine(zoomLevel, oldZoomLevel, stateMachine, position)
  oldZoomLevel = app.view.zoom.level



app.on "start", ->

  $(".content").append(app.view.zoom.el)
  app.view.zoom.activate()

  oldZoomLevel = app.view.zoom.level

  setActiveView(app.view.zoom.level)
  $(document.body).on("mousewheel", mouseWheelHandler)

  app.view.zoom.on(this, "change", setActiveView)
