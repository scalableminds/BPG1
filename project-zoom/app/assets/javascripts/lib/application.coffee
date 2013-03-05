### define
./event_mixin : EventMixin
jquery : $
underscore : _
async : async
###

class Application

  constructor : ->

    EventMixin.extend(this)


  addInitializer : (initializer) ->

    @on "initialize", initializer


  start : (options = {}) ->

    wrapperMaker = (initializer) -> 
      (callback) ->
        result = initializer(options, callback)
        if result? and _.isFunction(result.then) and _.isFunction(result.done)
          result.then(
            (arg) -> callback(null, arg)
            (err) -> callback(err)
          )
        return

    async.parallel(
      (@__callbacks.initialize ? []).map(wrapperMaker)
      (err) =>
        if err
          console.error(err)
          @trigger("initialize:error", err)
        else
          @trigger("initialize:after", options)
          @trigger("start", options)
    )
