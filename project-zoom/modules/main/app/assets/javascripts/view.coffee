### define
jquery : $
underscore : _
app : app
./view/zoom : Zoom
./view/process_view : ProcessView
###

app.addInitializer ->

  app.view = 
    zoom : new Zoom()


activeView = null
setActiveView = (zoomLevel) ->

  removeActiveView = ->
    oldActiveView = activeView
    activeView?.$el.remove()
    _.defer -> oldActiveView?.deactivate()
    activeView = null

  
  if .5 < zoomLevel <= 3
    unless activeView instanceof ProcessView
      removeActiveView()      
      activeView = new ProcessView(app.model.project)
      $(".content").append(activeView.el)

  if zoomLevel <= .5
    removeActiveView()

  return



app.on "start", ->

  $(".content").append(app.view.zoom.el)
  app.view.zoom.activate()

  setActiveView(app.view.zoom.level)
  app.view.zoom.on(this, "change", setActiveView)
