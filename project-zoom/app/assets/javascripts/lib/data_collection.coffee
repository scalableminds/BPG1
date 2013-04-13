### define
underscore : _
./event_mixin : EventMixin
###

class DataCollection

  constructor : ->

    EventMixin.extend(this)

    @items = []

    Object.defineProperty( this, "length", get : => @items.length )


  cloneShadow : ->


  fetch : (limit, offset) ->

    

  at : (index) ->

    @items[index]


  add : (items...) ->

    @items.push(items...)
    return

    
  remove : (items...) ->

    @items = _.without(@items, items...)
    return

