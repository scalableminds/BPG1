### define
underscore : _
async : async
./event_mixin : EventMixin
./request : Request
./change_accumulator : ChangeAccumulator
###

###
TODO:
  * json patch support
  * decycling events
###

class DataItem

  constructor : (json = {}, options = {}) ->

    EventMixin.extend(this)
    @trackChanges = _.memoize(@trackChanges)
    @changeAcc = new ChangeAccumulator()

    @on(@changeAcc, "change", @changeAcc.addChange)

    @attributes = {}
    @lazyAttributes = options.lazy ? {}

    @set(json)


  trackChanges : (key) -> (changeSet) =>

    @trigger("change", _.object([key], [changeSet]), this)


  get : (key, self, callback) ->

    if arguments.length == 1

      if key.indexOf("/") == -1
        @attributes[key]

      else
        remainingKey = key.substring(key.indexOf("/") + 1)
        key = key.substring(0, key.indexOf("/"))

        @attributes[key].get(remainingKey)

    else

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


  getLazy : (key, self, callback) ->

    callback = @dispatcher.register(this, self, null, callback)

    if _.isArray(@lazyAttributes[key])

      promise = $.when( @lazyAttributes[key].map(@requestLazy)... )

    else

      promise = @requestLazy(@lazyAttributes[key])

    promise.then(

      @dispatcher.register this, (result...) =>

        if _.isArray(@lazyAttributes[key])
          item = @set(key, result)
        else
          item = @set(key, result[0])

        delete @lazyAttributes[key]
        callback.oneShot(item)


      @dispatcher.register this, (result) =>

        callback.oneShot(undefined)

    )


  requestLazy : (url) ->

    if (result = DataItem.lazyCache[url])?
      result

    else
      Request.send(
        url : url
        method : "GET"
        dataType : "json"
      ).then(
        (result) ->
          DataItem.lazyCache[url] = 
            new $.Deferred()
              .resolve(DataItem.prepareValue(result))
              .promise()
      )


  getDirect : (key, self, callback) ->

    callback = @dispatcher.register(this, self, null, callback)

    if @lazyAttributes[key]
      # TODO
      @getLazy(key, self, callback.oneShot)
    
    else
      _.defer =>
        callback.oneShot(@attributes[key])



  set : (key, value) ->

    if _.isObject(key)

      @set(k, v) for k, v of key
      return

    else

      if key.indexOf("/") == -1

        @_set(key, value)

      else

        remainingKey = key.substring(key.indexOf("/") + 1)
        key = key.substring(0, key.indexOf("/"))
        this.attributes[key].set(remainingKey, value)



  _set : (key, value) ->

    if oldValue = @attributes[key]
      if oldValue instanceof DataItem or oldValue instanceof DataItem.Collection
        oldValue.off(this, "change", @trackChanges(key))

    if key[0] == "_"

      if _.isArray(value)
        @lazyAttributes[key.substring(1)] = value.map( (a) ->
          "/#{_.pluralize(key.substring(1))}/#{a}"
        )

      else
        @lazyAttributes[key.substring(1)] = "/#{_.pluralize(key.substring(1))}/#{value}"

    else

      value = DataItem.prepareValue(value)
      @attributes[key] = value

      if value instanceof DataItem or value instanceof DataItem.Collection
        value.on(this, "change", @trackChanges(key))

      @trigger("change:#{key}", value, this)
      @trigger("change", _.object([key], [value]), this)

    value


  unset : (key) ->

    if oldValue = @attributes[key]
      if oldValue instanceof DataItem or oldValue instanceof DataItem.Collection
        oldValue.off(this, "change", @trackChanges(key))

      delete @attributes[key]

      @trigger("change:#{key}", undefined, this)
      @trigger("change", _.object([key], [undefined]), this)

    return


  toObject : ->

    _.object(
      _.pairs(@attributes).map( ( [key, value] ) ->
        if value instanceof DataItem or value instanceof DataItem.Collection
          [key, value.toObject()]
        else
          [key, value]
      )
    )


  @prepareValue : (value) ->

    if _.isArray(value)
      new DataItem.Collection(value)

    else if _.isObject(value) and not (value instanceof DataItem) and not (value instanceof DataItem.Collection)
      
      new DataItem(value)

    else
      value


  @lazyCache : {}



class DataItem.Collection

  DEFAULT_LIMIT : 50

  constructor : (items = []) ->

    EventMixin.extend(this)

    if _.isString(items)
      @url = items
      items = []


    @items = []
    @parts = []

    for item in items
      @items.push(DataItem.prepareValue(item))

    Object.defineProperty( this, "length", get : => @items.length )


  trackChanges : (changeSet, item) =>

    index = _.findIndex(@items, item)
    @trigger("change", _.object([index], [changeSet]), this)


  fetch : (offset, limit) ->

    Request.send(
      url : "#{@url}?limit=#{limit}&offset=#{offset}"
      method : "GET"
      dataType : "json"
    ).then(

      @dispatcher.register this, (result) =>

        for item, i in result.content when i < result.limit
          @set(result.offset + i, item)

        @addParts(result.offset, result.limit)

        return

    )


  fetchNext : ->

    if lastPart = _.last(@parts)
      @fetch(lastPart.end, @DEFAULT_LIMIT)

    else
      @fetch(0, @DEFAULT_LIMIT)


  addParts : (offset, count) ->

    { parts } = this
    lastPart = null
    for part, i in parts
      
      if offset <= part.start <= offset + count or offset <= part.end <= offset + count

        part.start = Math.min(part.start, offset)
        part.end = Math.max(part.end, offset + count)

        if i + 1 < parts.length and parts[i + 1].start <= part.end
          part.end = Math.max(parts[i + 1].end, part.end)
          parts.splice(i + 1, 1)

        return

      if part.start > offset + count

        parts.splice(i, 0, start : offset, end : offset + count)
        return

      lastPart = part

    parts.push( start : offset, end : offset + count )
    return


  removeParts : (offset, count) ->




  get : (key, self, callback) ->

    if arguments.length == 1

      if key.indexOf("/") == -1
        @at(parseInt(key))

      else
        remainingKey = key.substring(key.indexOf("/") + 1)
        key = key.substring(0, key.indexOf("/"))

        @at( parseInt(key) ).get(remainingKey)

    else
    
      callback = @dispatcher.register(this, self, null, callback)
      
      if key.indexOf("/") == -1
        _.defer => callback.oneShot( @at(parseInt(key)) )

      else
        remainingKey = key.substring(key.indexOf("/") + 1)
        key = key.substring(0, key.indexOf("/"))

        @at( parseInt(key) ).get(remainingKey, self, (value) -> callback.oneShot( value ))

      return


  at : (index) ->

    @items[index]


  add : (items...) ->

    offset = @length - 1
    @set(@length, item) for item in items
    @addParts(offset, items.length)

    return


  set : (index = @length, item) ->

    item = DataItem.prepareValue(item)
    if item instanceof DataItem or item instanceof DataItem.Collection
      item.on(this, "change", @trackChanges)

    @items[index] = item
    @trigger("add", item, index, this)
    @trigger("change:#{index}", item, this)
    @trigger("change", _.object([index], [item]), this)

    
  remove : (items...) ->

    for item in items
      index = _.findIndex(@items, item)
      if item instanceof DataItem or item instanceof DataItem.Collection
        item.off(this, "change", @trackChanges)
      @items.splice(index, 1)
      @removeParts(index, 1)
      @trigger("remove", item, index, this) 
      @trigger("change:#{index}", undefined, this)
      @trigger("change", _.object([index], [undefined]), this)
    return


  toObject : ->

    @items.map( (item) ->
      if item instanceof DataItem or item instanceof DataItem.Collection
        item.toObject()
      else
        item
    )


DataItem
