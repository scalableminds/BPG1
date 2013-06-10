### define
underscore : _
jquery : $
backbone : Backbone
app : app
lib/data_item : DataItem
lib/request : Request
lib/utils : Utils
###

SAVE_THROTTLE = 10000
SAVE_DEBOUNCE = 500
SAVE_RETRY_TIMEOUT = 10000
SAVE_RETRY_COUNT = 20

ModelFunctions =

  prepareArtifacts : (project) ->

    artifacts = new DataItem.Collection("/projects/#{project.get("id")}/artifacts")
    artifacts.fetchNext().then(
      ->
        project.set("artifacts", artifacts)
    ) 

  prepareGraph : (project) ->

    (new $.Deferred (deferred) ->
      project.get("graphs", project, (graphCollection) ->

        unless graphCollection
          project.set("graphs", [])
          graphCollection = project.get("graphs")

        if graphCollection.length == 0
          DataItem.fetch("/projects/#{project.get("id")}/graphs", method : "POST").then( 
            (graph) ->
              graphCollection.add(graph)

              deferred.resolve(graph)
          )

        else
          deferred.resolve(graphCollection.at(0))
      )
      return

    ).then(
      (graph) ->

        isSaving = false

        graph.save = ->

          if isSaving
            isSaving.done -> 
              graph.isDirty = true
              graph.save()
            return

          patchData = app.model.project.get("graphs/0").patchAcc.flush()

          return if patchData.length == 0

          graph.trigger("save:start")

          isSaving = Utils.retryDeferred(
            ->
              Request.send(
                url : "/graphs/#{graph.get("group")}/#{graph.get("version")}"
                method : "PATCH"
                data : patchData
                dataType : "json"
              ).then( 
                ({ version }) -> 
                  graph.set("version", version, silent : true)
                  graph.isDirty = false
                  return
                ($xhr) ->
                  if $xhr.status == 400 
                    alert("Sorry. We couldn't save. (400)")
                    $.Deferred().resolve()
                  else
                    null
              )

            SAVE_RETRY_COUNT
            SAVE_RETRY_TIMEOUT

          )
            .fail( ->
              alert("Sorry. We couldn't save. (Retry timeout)")
            )
            .always( ->
              graph.trigger("save:done")
              isSaving = false
            )

        graph.on(graph, "patch:*", ->
          graph.isDirty = true
        )

        graph.on(graph, "patch:*", _.debounce(
          -> graph.save()
          SAVE_DEBOUNCE
        ))

        $(window).on("beforeunload", ->
          if graph.isDirty
            graph.save()
            return "We haven't saved yet. Please wait a little longer."
        )

    )


app.addInitializer (options, callback) ->

  model =
    projects : new DataItem.Collection("/projects")
    tags : new DataItem.Collection("/tags")
    project : null


  $.when(

    model.projects.fetchNext().then( 
      ->
        model.project = model.projects.at(0)

        $.when(
          ModelFunctions.prepareGraph(model.project)
          ModelFunctions.prepareArtifacts(model.project)
        )

    )

    model.tags.fetchNext()

  ).then(
    ->

      app.model = model

      callback()
  )

    

  return
