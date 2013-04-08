### define
backbone : Backbone
app : app
jquery : $
./component/artifact_finder : ArtifactFinder

###

app.addInitializer (options, callback) ->

  model = new Backbone.DeepModel(
    test :
      xyz : 123
  )

  model.set("test.zyx", 321)

  artifactFinder = new ArtifactFinder()

  $("#artifactFinder").append(artifactFinder.domElement)



  callback()
