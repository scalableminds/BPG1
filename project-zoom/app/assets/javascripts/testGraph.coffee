### define
app : app
jquery : $
view/process_view : ProcessView
###

app.on "start", ->

  view = new ProcessView()
