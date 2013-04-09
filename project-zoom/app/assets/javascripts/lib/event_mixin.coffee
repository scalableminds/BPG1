### define 
underscore : _
./event_dispatcher : EventDispatcher
###

class EventMixin

  constructor : (dispatcher = EventMixin.dispatcher) ->

    @__callbacks = {}
    @__boundObjects = {}
    @__dispatcher = dispatcher
    EventMixin.ensureUid(this)


  on : (self, type, callback) ->

    if _.isObject(self) and arguments.length == 1

      @on(null, key, value) for key, value of self


    else if _.isString(self) and arguments.length == 2

      @on(null, self, type)


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

        @__dispatcher.register(boundObjectEntry)


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


    else if _.isString(self) and arguments.length == 2

      @off(null, self, type)

    
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

        @__dispatcher.unregister(this, self, type, callback)

    this


  times : (self, type, callback, count) ->

    return this if count < 1

    wrappedCallback = (args...) =>

      callback(args...)
      @off(self, type, wrappedCallback) if --count == 0
      return

    @on(self, type, wrappedCallback)


  one : (self, type, callback) ->

    @times(self, type, callback, 1)


  trigger : (type, args...) ->

    if @__callbacks[type]?
      for callback in @__callbacks[type]
        callback.apply(this, args)

    this


  @dispatcher : new EventDispatcher


  @extend : (obj, dispatcher) ->

    _.extend(obj, new this(dispatcher))


  @ensureUid : (obj) ->

    unless _.isString(obj.__uid)
      obj.__uid = _.uniqueId("eventMixin")
    obj
