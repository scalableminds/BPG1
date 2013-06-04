### define
jquery : $
underscore : _
app : app
./view/zoom : Zoom
###

app.addInitializer ->

  app.view = 
    zoom : new Zoom()


app.on "start", ->

  $(".content").append(app.view.zoom.el)
  app.view.zoom.activate()