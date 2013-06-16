### define
jquery : $
underscore : _
app : app
hammer : Hammer
./view/zoom : Zoom
./view/wheel : Wheel
./view/projects_overview_view : ProjectsOverviewView
./view/process_view : ProcessView
./view/details_view : DetailsView
./view/view_wrapper : ViewWrapper
lib/exec_queue : ExecQueue
lib/range_switch : RangeSwitch
lib/event_mixin : EventMixin
###


app.addInitializer ->

  app.view = {}

  app.view.zoom = new Zoom()
  app.view.overview = new ViewWrapper(
    -> new ProjectsOverviewView(app.model.projects)
    (level) -> Math.max(level * .3 + .3, .3)
  )

  app.view.details = new ViewWrapper(
    -> new DetailsView(app.model.project)
  )

  app.view.process = new ViewWrapper(
      -> new ProcessView(app.model.project)
      (level) -> 
        # 100 steps starting from .2
        Math.max(
          if level > 60
            (level - 50) * .1 + 1
          else
            (level - 40) * .0833 + .4
          .4
        )
    )

  app.view.wheel = new Wheel(document.body)

  app.view.wheel.on("delta", app.view.zoom.changeZoom)



switchView = RangeSwitch(

  ExecQueue( (next) -> -> _.defer(next) )

  "0 <= x < 10" : ->

    app.view.overview.activate()

    app.view.details.kill()
    app.view.process.kill()
    app.view.wheel.deactivate()
    

  "10 <= x < 20" : (level, position) ->

    app.view.overview.deactivate()
    
    normalizedLevel = (level - 10) / 10

    app.view.details.deactivate()
    app.view.details.setZoom(normalizedLevel, position)

    app.view.process.kill()
    app.view.wheel.activate()


  "20 <= x < 30" : ->

    app.view.overview.kill()

    app.view.details.activate()
    app.view.details.resetZoom()

    app.view.process.kill()
    app.view.wheel.activate()


  "30 <= x < 40" : (level, position) ->

    app.view.overview.kill()

    app.view.details.deactivate()
    app.view.details.resetZoom()

    normalizedLevel = (level - 30) / 10

    app.view.process.deactivate()
    app.view.process.setZoom(normalizedLevel, position)

    app.view.wheel.activate()


  "40 <= x <= 150" : (level) ->

    app.view.overview.kill()
    app.view.details.kill()

    app.view.process.resetZoom()
    app.view.process.activate()

    app.view.wheel.deactivate()

)


app.on "start", ->

  $(".content").append(app.view.zoom.el)
  app.view.zoom.activate()

  switchView(app.view.zoom.level)

  app.view.zoom.on(this, "change", switchView)
