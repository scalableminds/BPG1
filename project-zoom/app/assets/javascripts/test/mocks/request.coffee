### define 
jquery : $
###

Request =

	deferreds : []

	send : ->

		deferred = new $.Deferred()

		@deferreds.push(deferred)

		deferred.always => @deferreds.splice(@deferreds.indexOf(deferred), 1); return

		deferred


	trigger : (data) ->

		@deferreds.forEach( (a) -> a.resolve(data) )