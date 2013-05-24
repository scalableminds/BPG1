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
      model.project = model.projects.find((p) -> p.get("id") == "519b693d030655c8752c2983")

      new $.Deferred (deferred) ->
        model.project.get("graphs/0", model.project, deferred.resolve)
        return

  ).then(
    ->

      app.model = model

      callback()
  )

    

  return
