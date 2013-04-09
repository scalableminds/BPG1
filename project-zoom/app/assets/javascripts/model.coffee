### define
underscore : _
backbone : Backbone
app : app
###

class Model

	constructor : ->



app.addInitializer (callback) ->

	app.model = new Model()
	callback()