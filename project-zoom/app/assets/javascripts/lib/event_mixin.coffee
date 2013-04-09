### define 
underscore : _
###

class EventMixin

  constructor : ->

    @__callbacks = {}
    @__boundObjects = {}
    EventMixin.ensureUid(this)


  on : (self, type, callback) ->

    if _.isObject(self) and arguments.length == 1

      @on(null, key, value) for key, value of self


    else if _.isObject(type) and arguments.length == 2

      @on(self, key, value) for key, value of type


    else if _.isArray(callback)

      @on(self, type, singleCallback) for singleCallback in callback


    else

      if self?

        self = EventMixin.ensureUid(self)

        boundObjectEntry = { type, callback, sender : this, target : self }

        unless @__boundObjects[self.__uid]?
          @__boundObjects[self.__uid] = [ boundObjectEntry ]

        else
          @__boundObjects[self.__uid].push( boundObjectEntry )


      unless _.isArray(@__callbacks[type])
        @__callbacks[type] = [ callback ]

      else
        @__callbacks[type].push(callback)

    this


  off : (self, type, callback) ->

    if arguments.length == 1

      if self.__uid?
        @off(self, type, callback) for { type, callback } in @__boundObjects[self.__uid]

      else
        @off(null, key, value) for key, value of self


    else if _.isObject(type)

      @off(self, key, value) for key, value of type


    else if _.isArray(callback)

      @off(self, type, singleCallback) for singleCallback in callback


    else

      callbackArray = @__callbacks[type]

      if _.isArray(@__callbacks[type])

        _.removeElement(@__callbacks[type], callback)
      
      if self?
        
        boundObjectArray = @__boundObjects[self.__uid]
        _.removeElementAt(boundObjectArray, _.findIndex( (a) -> a.callback == callback ))

        delete @__boundObjects[self.__uid] if boundObjectArray.length == 0


    this


  one : (self, type, callback) ->

    wrapCallback = (callback) =>
      (args...) =>

        callback(args...)
        @off(self, type, wrappedCallback)

    @on(self, type, wrapCallback(callback))


  trigger : (type, args...) ->

    if _.isObject(type)
      map = type
      @trigger(type, arg) for type, arg of map

    else
      
      if _.isArray(@__callbacks[type])
        for callback in @__callbacks[type]
          callback.apply(this, args)

    this


  @extend : (obj) ->

    _.extend(obj, new this())


  @ensureUid : (obj) ->

    unless _.isString(obj.__uid)
      obj.__uid = _.uniqueId("eventMixin")
    obj
