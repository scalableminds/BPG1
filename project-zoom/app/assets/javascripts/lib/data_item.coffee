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
          @attributes[key] = new DataItem.Collection(value, this)

        else if _.isObject(value)
          @attributes[key] = new DataItem(value, this)

        else
          @attributes[key] = value

          @trigger("change:#{key}", value, this)

          @trigger("change", _.object([key],[value]), this)

      else

        remainingKey = key.substring(key.indexOf("/") + 1)
        key = key.substring(0, key.indexOf("/"))
        this.attributes[key].set(remainingKey, value)




class DataItem.Collection

  DEFAULT_LIMIT : 50

  constructor : (items = [], parent) ->

    EventMixin.extend(this)

    @items = []
    @parts = []

    @items.push(new DataItem(item, this)) for item in items

    Object.defineProperty( this, "length", get : => @items.length )


  cloneShadow : ->


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
      @items.push(item)
      @trigger("add", item, this)
      @trigger("change:#{index}", item, this)
      @trigger("change", _.object([index], [item]), this)
    return

    
  remove : (items...) ->

    for item in items
      index = _.findIndex(@items, item)
      @items.splice(index, 1)
      @trigger("remove", item, this) 
      @trigger("change:#{index}", undefined, this)
      @trigger("change", _.object([index], [undefined]), this)
    return


DataItem
