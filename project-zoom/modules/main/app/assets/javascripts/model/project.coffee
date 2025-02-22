### define
underscore : _
jquery : $
lib/data_item : DataItem
lib/json_patch_accumulator : JsonPatchAccumulator
lib/request : Request
lib/utils : Utils
###

SAVE_THROTTLE = 10000
SAVE_DEBOUNCE = 500
SAVE_RETRY_TIMEOUT = 10000
SAVE_RETRY_COUNT = 20

VIEW_ONLY = !!window.location.href.match(/viewer/)

Project =

  load : (project) ->

    return project.loaded if project.loaded

    project.loaded = $.when(
      @prepareTags(project)
      @prepareParticipants(project)
      @prepareArtifacts(project)
      @prepareGraph(project)
    )


  prepareTags : (project) ->

    (new $.Deferred (deferred) ->
      project.get("tags", project, (tagCollection) ->
        deferred.resolve(tagCollection)
      )
    )


  prepareParticipants : (project) ->

    $.when(
      project.get("participants").map((participant) -> 
        (new $.Deferred (deferred) ->
          participant.get("user", project, (user) ->
            deferred.resolve(user)
          )
        )
      )...
    )


  prepareArtifacts : (project) ->

    artifacts = new DataItem.Collection("/projects/#{project.get("id")}/artifacts")
    artifacts.fetchNext().then(
      ->
        project.set("artifacts", artifacts)
    )


  prepareGraph : (project) ->

    new $.Deferred (deferred) ->

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

      deferred.then(
        (graph) ->

          patchAcc = JsonPatchAccumulator.attach(graph)

          isSaving = false

          graph.save = ->

            if isSaving
              isSaving.done -> 
                graph.isDirty = true
                graph.save()
              return

            patchData = patchAcc.flush()

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

          if VIEW_ONLY

            webSocket = new WebSocket("ws://#{window.location.host}/projects/#{project.get("id")}/updateChannel")
            webSocket.addEventListener("message", (event) ->

              data = JSON.parse(event.data)
              if data.collection == "graphs" and data.operation == "patch"
                graph.applyPatches(data.value)
                console.log(data.value)

            )

          else

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

          graph

      )
    