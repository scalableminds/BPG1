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
SAVE_RETRY = 10000

ModelFunctions =
  prepareGraph : (project) ->

    (new $.Deferred (deferred) ->
      project.get("graphs", project, (graphCollection) ->
        if graphCollection.length == 0
          DataItem.fetch("/projects/#{project.get("id")}/graphs").then( 
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
        patchBuffer = []

        graph.save = ->

          if isSaving
            isSaving.done -> graph.save()
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
                  graph.set("version", version); return
                ($xhr) ->
                  if $xhr.status == 400 
                    alert("Sorry. We couldn't save.")
                    $.Deferred().resolve()
                  else
                    null
              )

            undefined
            SAVE_RETRY

          ).always ->
            isSaving = false


        graph.on(graph, "patch:*", _.throttle(
          -> graph.save()
          SAVE_THROTTLE
        ))

    )


app.addInitializer (options, callback) ->

  model =
    projects : new DataItem.Collection("/projects")
    project : null


  model.projects.fetchNext().then( 
    ->

      model.projects.get("0/participants/0/user", this, (item) -> console.log(item))
      model.project = model.projects.find((p) -> p.get("id") == "519b693d030655c8752c2973")

      ModelFunctions.prepareGraph(model.project)

  ).then(
    ->

      app.model = model

      callback()
  )

    

  return
