### define
app : app
jquery : $
view/process_view : ProcessView
###

app.on "start", ->

  app.model.project.set(
  	graphs : [
  		nodes : for i in [0..5]
  			{
  				id : Math.floor(Math.random() * 1e6)
  				x : i * 70
  				y : i * 70
  				artifact : null
  			}
  	]
  )
  view = new ProcessView(app.model.project)
