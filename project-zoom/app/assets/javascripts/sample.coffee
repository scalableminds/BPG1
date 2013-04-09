### define
backbone : Backbone
app : app
jquery : $
./component/artifact_finder : ArtifactFinder

###

app.addInitializer (options, callback) ->


  artifactFinder = new ArtifactFinder()

  $("#artifactFinder").append(artifactFinder.domElement)



  callback()
