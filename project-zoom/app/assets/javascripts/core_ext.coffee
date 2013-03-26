### define
d3 : d3
###


d3.selection.prototype.delegate = (evt, target, handler) ->
  this.on(evt, delegate(handler, target))


#delegate an event handler to only fire for specific elements
delegate = (handler, target) ->

  ->
    evtTarget = d3.event.target
    if $(evtTarget).is(target)
      handler.call(evtTarget, evtTarget.__data__)

return true