### define
underscore : _
./event_mixin : EventMixin
###

class DataItem

  constructor : ->

    EventMixin.extend(this)

    @attributes = {}


  get : (key, self, callback) ->

    callback = @dispatcher.register(this, self, null, callback)
    _.defer =>
      callback.oneShot(@attributes[key])


  set : (key, value) ->

    if _.isObject(key)

      @set(k, v) for k, v of key

    else

      @attributes[key] = value

    
