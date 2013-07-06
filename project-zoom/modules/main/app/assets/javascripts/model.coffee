### define
underscore : _
jquery : $
app : app
lib/data_item : DataItem
lib/request : Request
lib/utils : Utils

./model/project : Project
###

ModelFunctions =

  Project : Project


app.addInitializer (options, callback) ->

  model =
    functions : ModelFunctions
    projects : new DataItem.Collection("/projects")
    tags : new DataItem.Collection("/tags")
    project : null
    setProject : (project) ->

      return new $.Deferred().resolve(project) if project == model.project

      model.project = project

      unless project instanceof DataItem
        project = model.projects.find( (a) -> a.get("id") == project.id )

      ModelFunctions.Project.load(project)



  $.when(

    model.projects.loaded = model.projects.fetchNext().then( 
      ->
        model.project = model.projects.at(0)
        $.when(
          model.projects.map(ModelFunctions.Project.prepareTags)...
          ModelFunctions.Project.load(model.project)
        )
    )

    model.tags.fetchNext()

  ).then(
    ->

      app.model = model

      callback()
  )

    

  return
