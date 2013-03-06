### define
backbone : Backbone
app : app
jquery : $
###

app.addInitializer (options, callback) ->

  model = new Backbone.DeepModel( 
    test : 
      xyz : 123
  )

  model.set("test.zyx", 321)

  callback()
