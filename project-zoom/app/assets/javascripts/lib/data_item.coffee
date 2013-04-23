### define
underscore : _
./event_mixin : EventMixin
async : async
./request : Request
###

###
TODO:
  * change sets/events
  * Lazy loading/schema
  * json patch support
###

class ChangeAccumulator

  constructor : ->

    @changes = []


  addChange : (change) =>

    change = _.object(
      _.pairs(change).map( ( [key, value] ) ->
        [key, if value?.toJSON then value.toJSON() else value]
      )
    )
    Object.defineProperty(change, "__timestamp", value : Date.now())
    @changes.push(change)
    return


  flush : ->

    merge = (source, target) ->
      
      _.forOwn(source, (value, key) -> 
        if _.isObject(value)
          target[key] = {} unless target[key]?
          merge(value, target[key])
        else
          target[key] = value
      )
      target

    changeSet = {}
    merge(change, changeSet) for change in @changes
    Object.defineProperty(changeSet, "__timestamp", value : _.max(@changes, "__timestamp").__timestamp)
    @changes.length = 0
    changeSet



class DataItem

  constructor : (json = {}, @parent) ->

    EventMixin.extend(this)
    @trackChanges = _.memoize(@trackChanges)
    @changeAcc = new ChangeAccumulator()

    @on(@changeAcc, "change", @changeAcc.addChange)

    @attributes = {}
    @lazyAttributes = {}

    @set(json)


  trackChanges : (key) -> (changeSet) =>

    @trigger("change", _.object([key], [changeSet]), this)


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
          @_set(key, new DataItem.Collection(value, this))

        else if _.isObject(value)
          
          @_set(key, new DataItem(value, this))

        else
          @_set(key, value)

      else

        remainingKey = key.substring(key.indexOf("/") + 1)
        key = key.substring(0, key.indexOf("/"))
        this.attributes[key].set(remainingKey, value)



  _set : (key, value) ->

    if oldValue = @attributes[key]
      if oldValue instanceof DataItem or oldValue instanceof DataItem.Collection
        oldValue.off(this, "change", @trackChanges(key))

    @attributes[key] = value

    if value instanceof DataItem or value instanceof DataItem.Collection
      value.on(this, "change", @trackChanges(key))

    @trigger("change:#{key}", value, this)
    @trigger("change", _.object([key], [value]), this)


  unset : (key) ->

    if oldValue = @attributes[key]
      if oldValue instanceof DataItem or oldValue instanceof DataItem.Collection
        oldValue.off(this, "change", @trackChanges(key))

      delete @attributes[key]

      @trigger("change:#{key}", undefined, this)
      @trigger("change", _.object([key], [undefined]), this)

    return


  toJSON : ->

    _.object(
      _.pairs(@attributes).map( ( [key, value] ) ->
        if value instanceof DataItem or value instanceof DataItem.Collection
          [key, value.toJSON()]
        else
          [key, value]
      )
    )






class DataItem.Collection

  DEFAULT_LIMIT : 50

  constructor : (items = [], parent) ->

    EventMixin.extend(this)

    @items = []
    @parts = []

    @items.push(new DataItem(item, this)) for item in items

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

        for item, i in result.items when i < result.limit
          @items[result.offset + i] = new DataItem(item, this)

        @extendParts(result.offset, result.limit)
        return

    )


  fetchNext : ->

    if lastPart = _.last(@parts)
      @fetch(lastPart.end, @DEFAULT_LIMIT)

    else
      @fetch(0, @DEFAULT_LIMIT)


  extendParts : (offset, count) ->

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


  get : (key, self, callback) ->

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

    for item in items
      index = @length
      if item instanceof DataItem or item instanceof DataItem.Collection
        item.on(this, "change", @trackChanges)
      @items.push(item)
      @trigger("add", item, this)
      @trigger("change:#{index}", item, this)
      @trigger("change", _.object([index], [item]), this)
    return

    
  remove : (items...) ->

    for item in items
      index = _.findIndex(@items, item)
      if item instanceof DataItem or item instanceof DataItem.Collection
        item.off(this, "change", @trackChanges)
      @items.splice(index, 1)
      @trigger("remove", item, this) 
      @trigger("change:#{index}", undefined, this)
      @trigger("change", _.object([index], [undefined]), this)
    return


  toJSON : ->

    @items.map( (item) ->
      if item instanceof DataItem or item instanceof DataItem.Collection
        item.toJSON()
      else
        item
    )


DataItem
