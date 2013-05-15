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


  model.projects.fetchNext().done ->

    model.projects.get("0/participants/0/user", this, (item) -> console.log(item))
    model.project = model.projects.at(0)

    app.model = model

    callback()

  return
