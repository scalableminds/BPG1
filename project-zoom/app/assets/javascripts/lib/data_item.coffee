### define
underscore : _
./event_mixin : EventMixin
async : async
###

###
TODO:
  * nested DataCollection
  * change sets/events
  * Lazy loading/schema
  * json patch support
###


class DataItem

  constructor : (json = {}, @parent) ->

    EventMixin.extend(this)

    @attributes = {}
    @lazyAttributes = {}

    @set(json)


  get : (key, self, callback) ->

    callback = @dispatcher.register(this, self, null, callback)
    
    if key.indexOf("/") == -1
      @getDirect(key, self, (value) -> callback.oneShot(value) )

    else

      remainingKey = key.substring(key.indexOf("/") + 1)
      key = key.substring(0, key.indexOf("/"))

      @getDirect(key, self, (value) -> 
        value.get(remainingKey, self, (value) -> callback.oneShot(value) )
      )

    return  


  getDirect : (key, self, callback) ->

    callback = @dispatcher.register(this, self, null, callback)

    if @lazyAttributes[key]
      # TODO
      _.defer =>
        delete @lazyAttributes[key]
        @attributes[key] = value = "dummy"
        callback.oneShot(value)
    
    else
      _.defer =>
        callback.oneShot(@attributes[key])



  set : (key, value) ->

    if _.isObject(key)

      @set(k, v) for k, v of key

    else

      if key.indexOf("/") == -1

        if _.isArray(value)
          @attributes[key] = new DataCollection(value, this)

        else if _.isObject(value)
          @attributes[key] = new DataItem(value, this)

        else
          @attributes[key] = value

      else

        remainingKey = key.substring(key.indexOf("/") + 1)
        key = key.substring(0, key.indexOf("/"))
        this.attributes[key].set(remainingKey, value)



      
