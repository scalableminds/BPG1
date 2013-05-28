### define
app : app
jquery : $
view/process_view : ProcessView
###

app.on "start", ->

  # app.model.project.set(
  #   graphs : [
  #     nodes : for i in [0..5]
  #       {
  #         id : 1e5+i
  #         position : 
  #           x : i * 70
  #           y : i * 70
  #         artifact : null
  #         payload : 
  #           id : "519b693d030655c8752c2973"
  #         typ : "project"
  #       }

  #     edges : [
  #       id : 1e5 * 2
  #       from : 1e5 + 1
  #       to : 1e5 + 2
  #     ]

  #     clusters : []
  #   ]
  # )
  # delete app.model.project.lazyAttributes.graphs
  view = new ProcessView(app.model.project)
