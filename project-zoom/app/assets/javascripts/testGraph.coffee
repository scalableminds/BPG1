### define
app : app
jquery : $
view/process_view : ProcessView
###

app.addInitializer (options, callback) ->

  view = new ProcessView()

  callback()
