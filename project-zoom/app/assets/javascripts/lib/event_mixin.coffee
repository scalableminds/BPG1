### define 
underscore : _
./event_dispatcher : EventDispatcher
###

class EventMixin

  constructor : (@dispatcher = EventMixin.dispatcher) ->

    @__callbacks = {}
    EventDispatcher.ensureUid(this)


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

        @dispatcher.register(this, self, type, callback)


      unless _.isArray(@__callbacks[type])
        @__callbacks[type] = [ callback ]

      else
        @__callbacks[type].push(callback)

    this


  off : (self, type, callback) ->

    if arguments.length == 1

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

        @dispatcher.unregister(this, self, type, callback)

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



  @extend : (obj, dispatcher) ->

    mixin = new EventMixin(dispatcher)

    _.forOwn(EventMixin.prototype, (func, key) -> obj[key] = _.bind(func, mixin) )
    
    obj.dispatcher = mixin.dispatcher

    Object.defineProperty(obj, "__uid", value : mixin.__uid )

    obj


  @dispatcher : new EventDispatcher


