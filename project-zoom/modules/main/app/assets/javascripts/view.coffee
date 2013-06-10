### define
jquery : $
underscore : _
app : app
hammer : Hammer
./view/zoom : Zoom
./view/process_view : ProcessView
lib/exec_queue : ExecQueue
###

class ViewHandler

  constructor : (@maker) ->

  view : null

  make : (args...) ->

    unless @view
      @view = @maker(args...)
      $(".content").append(@view.el)


  kill : ->

    if @view
      @deactivate()
      @view.$el.remove()
      @view = null


  activate : ->

    @make()
    unless @view.isActivated
      @view.activate()
    @view.$el.removeClass("inactive")


  deactivate : ->

    @make()
    if @view.isActivated
      @view.deactivate()
    @view.$el.addClass("inactive")


app.addInitializer ->

  app.view = 
    zoom : new Zoom()
    process : _.extend(
      new ViewHandler(-> new ProcessView(app.model.project))
      setZoom : (scale, position) ->

        @make()
        @view.$el.css(
          "transform" : "scale(#{scale})"
          "transformOrigin" : if position then "#{position[0]}px #{position[1]}px" else ""
        )

      resetZoom : ->

        @make()
        @view.$el.css(
          "transform" : ""
          "transformOrigin" : ""
        )
    )

    wheel :
      isActivated : false

      activate : ->

        unless @isActivated
          $(document.body).on("mousewheel", mouseWheelHandler)
          @isActivated = true


      deactivate : ->

        if @isActivated
          $(document.body).off("mousewheel", mouseWheelHandler)
          @isActivated = false





stateMachine =

  "0 <= x < .2" : ->

    app.view.process.kill()

    app.view.wheel.activate()


  ".2 <= x < .5" : (level, position) ->

    normalizedLevel = (level - .2) / .3

    app.view.process.deactivate()
    app.view.process.setZoom(normalizedLevel, position)

    app.view.wheel.activate()


  ".5 <= x <= 3" : (level) ->

    app.view.process.resetZoom()
    app.view.process.activate()

    app.view.wheel.deactivate()
   


execQueue = ExecQueue (next) -> -> _.defer(next)


execStateMachine = (rules, value, otherArgs...) ->

  _.forOwn(rules, (funcs, rule) ->

    [x, a0, cmp0, cmp1, a1] = rule.match(/^([\d\.]+)\s*([<>=]+)\s*x\s*([<>=]+)\s*([\d\.]+)$/)

    comparator = new Function("value", "return #{a0} #{cmp0} value && value #{cmp0} #{a1};")

    if comparator(value)
      funcs = [ funcs ] unless _.isArray(funcs)
      funcs.forEach( (func) -> 
        execQueue -> func(value, otherArgs...)
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


setActiveView = _.partial(execStateMachine, stateMachine)

app.on "start", ->

  $(".content").append(app.view.zoom.el)
  app.view.zoom.activate()

  setActiveView(app.view.zoom.level)

  app.view.zoom.on(this, "change", setActiveView)
