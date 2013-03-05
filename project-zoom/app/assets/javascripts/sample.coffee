### define
app : app
jquery : $
###

app.addInitializer (options, callback) ->

  alert(options.test)
  d = new $.Deferred()
  setTimeout(
    -> d.resolve()
    2000
  )
  d.promise()
