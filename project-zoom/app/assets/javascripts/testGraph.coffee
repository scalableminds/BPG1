### define
app : app
jquery : $
view/view : View
###

app.addInitializer (options, callback) ->

  view = new View()

  callback()
