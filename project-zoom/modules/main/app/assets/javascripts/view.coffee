### define
jquery : $
underscore : _
app : app
hammer : Hammer
./view/zoom : Zoom
./view/wheel : Wheel
./view/process_view : ProcessView
./view/view_wrapper : ViewWrapper
lib/exec_queue : ExecQueue
lib/range_switch : RangeSwitch
lib/event_mixin : EventMixin
###


app.addInitializer ->

  app.view = {}

  app.view.zoom = new Zoom()
  app.view.process = new ViewWrapper(
      -> new ProcessView(app.model.project)
      (level) -> level - .5
    )

  app.view.wheel = new Wheel(document.body)

  app.view.wheel.on("delta", app.view.zoom.changeZoom)



switchView = RangeSwitch(

  ExecQueue( (next) -> -> _.defer(next) )

  "0 <= x < .1" : ->

    console.log "0-.1", arguments[0]

    app.view.process.kill()

    app.view.wheel.activate()


  ".1 <= x < 1" : (level, position) ->

    console.log ".1-1", arguments[0]

    normalizedLevel = (level - .1) / .9

    app.view.process.deactivate()
    app.view.process.setZoom(normalizedLevel, position)

    app.view.wheel.activate()


  "1 <= x <= 10" : (level) ->

    console.log "1-10", arguments[0]

    app.view.process.resetZoom()
    app.view.process.activate()

    app.view.wheel.deactivate()

)


app.on "start", ->

  $(".content").append(app.view.zoom.el)
  app.view.zoom.activate()

  switchView(app.view.zoom.level)

  app.view.zoom.on(this, "change", switchView)
