### define
jquery : $
underscore : _
###

Utils =

  retryDeferred : (func, retryCount = -1, retryTimeout = -1) ->

    deferred = new $.Deferred()

    doTry = ->

      return if deferred.state() == "rejected"

      result = func()
      if _.isFunction(result.promise)

        result.then(

          (args...) -> deferred.resolve(args...)

          -> 
            retryCount-- if retryCount > 0
            if retryCount == 0
              deferred.reject()
            else
              if retryTimeout < 0
                doTry()
              else
                setTimeout(doTry, retryTimeout)


          (args...) -> deferred.notify(args...)

        )

      else
        throw "Return values was no promise."

    doTry()

    _.extend(deferred.promise(), stop : -> deferred.reject())
