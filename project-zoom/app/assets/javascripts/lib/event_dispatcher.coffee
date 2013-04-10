### define
underscore : _
###

class EventDispatcher

  constructor : ->

    @boundObjects = {}


  addObjectEntry : (obj, entry) ->

    objectsEntries = @boundObjects[obj.__uid]

    if objectsEntries?
      objectsEntries.push(entry)

    else
      @boundObjects[obj.__uid] = [ entry ]

    return


  removeObjectEntry : (obj, entry) ->

    objectsEntries = @boundObjects[obj.__uid]
    _.removeElement(objectsEntries, entry)

    if objectsEntries.length == 0
      delete @boundObjects[obj.__uid]

    else
      @boundObjects[obj.__uid] = objectsEntries

    return


  register : (sender, target, type, callback) ->

    if arguments.length == 1
      entry = sender
    else
      entry = { sender, target, type, callback }

    EventDispatcher.ensureUid(sender)
    EventDispatcher.ensureUid(target)

    callback = entry.callback
    entry.off = _.once => 
      
      callback = ->
      @removeObjectEntry(entry.sender, entry)
      @removeObjectEntry(entry.target, entry)

      return

    @addObjectEntry(entry.sender, entry)
    @addObjectEntry(entry.target, entry)

    _.extend(
      (args...) -> callback(args...)
      off : entry.off
      oneShot : (args...) -> 
        result = callback(args...)
        entry.off()
        result
    )


  unregister : (sender, target, type, callback) ->

    if arguments.length == 1
      similarEntry = sender
    else
      similarEntry = { sender, target, type, callback }

    senderObjectEntries = @boundObjects[sender.__uid]
    entry = _.find(senderObjectEntries, similarEntry)

    entry?.off()

    return


  unregisterAll : (self) ->

    objectsEntries = @boundObjects[self.__uid]
    
    for entry in objectsEntries

      if entry.sender == self
        self.off(entry.target, entry.type, entry.callback)

      else
        entry.sender.off(self, entry.type, entry.callback)

    return


  @ensureUid : (obj) ->

    unless _.isString(obj.__uid)
      Object.defineProperty( obj, "__uid", value : _.uniqueId("dispatcher") )
    obj
    
