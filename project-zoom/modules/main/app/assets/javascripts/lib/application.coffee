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


  start : (options = {}, callback) ->

    if _.isFunction(options) and not callback?
      callback = options
      options = {}

    wrapperMaker = (initializer) -> 
      (callback) ->
        
        switch initializer.length ? 2

          when 0

            result = initializer()
            
            if result? and _.isFunction(result.promise)
              result.then(
                (arg) -> callback(null, arg)
                (err) -> callback(err)
              )
            else
              _.defer callback

          when 1

            result = initializer(options)

            if result? and _.isFunction(result.promise)
              result.then(
                (arg) -> callback(null, arg)
                (err) -> callback(err)
              )
            else
              _.defer callback

          else

            result = initializer(options, callback)

            if result? and _.isFunction(result.promise)
              _.defer -> result.then(
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
          callback(options) if callback?
    )
