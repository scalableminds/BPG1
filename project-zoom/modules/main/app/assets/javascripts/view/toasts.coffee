### define
jquery : $
app : app
###

app.on "start", ->

	app.model.project.get("graphs/0").on(this, 
		"save:start" : -> $("#toasts").html("""<div class="toast"><i class="icon-spin icon-refresh"></i> Saving</div>""")
		"save:done" : -> $("#toasts").html("")
	)