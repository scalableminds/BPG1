### define
underscore : _
backbone : Backbone
app : app
lib/data_item : DataItem
###

app.addInitializer (options, callback) ->

  model =
    projects : new DataItem.Collection("/projects")
    project : null


  model.projects.fetchNext().then( 
    ->

      model.projects.get("0/participants/0/user", this, (item) -> console.log(item))
      model.project = model.projects.find((p) -> p.get("id") == "519b6ce19ad95e36e749a6b2")

      new $.Deferred (deferred) ->
        if model.project.lazyAttributes.graphs.length == 0
          DataItem.fetch("/projects/#{model.project.get("id")}/graphs").then( 
            (graph) ->
              model.project.get("graphs", model.project, (graphCollection) ->
                graphCollection.add(graph)
                deferred.resolve()
              )
          )
          
        else
          model.project.get("graphs/0", model.project, deferred.resolve)

        return

  ).then(
    ->

      app.model = model

      callback()
  )

    

  return
