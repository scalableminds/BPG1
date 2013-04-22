### define
underscore : _
./event_mixin : EventMixin
lib/request : Request
./data_item : DataItem
###

class DataCollection

  DEFAULT_LIMIT : 50

  constructor : (@url, parent) ->

    EventMixin.extend(this)

    @items = []
    @parts = []

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


  at : (index) ->

    @items[index]


  add : (items...) ->

    @items.push(items...)
    return

    
  remove : (items...) ->

    @items = _.without(@items, items...)
    return

