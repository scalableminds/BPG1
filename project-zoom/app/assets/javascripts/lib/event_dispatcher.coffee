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


  removeObjectEntry : (obj, similarEntry) ->

    objectsEntries = @boundObjects[obj.__uid]
    objectsEntries = _.reject(objectsEntries, similarEntry)

    if objectsEntries.length == 0
      delete @boundObjects[obj.__uid]

    else
      @boundObjects[obj.__uid] = objectsEntries

    return


  register : (entry) ->

    if arguments.length == 4
      entry =
        sender : arguments[0]
        target : arguments[1]
        type : arguments[2]
        callback : arguments[3]

    @addObjectEntry(entry.sender, entry)
    @addObjectEntry(entry.target, entry)

    return


  unregister : (sender, target, type, callback) ->

    if arguments.length == 1
      entry = sender
    else
      entry = { sender, target, type, callback }

    @removeObjectEntry(sender, entry)
    @removeObjectEntry(target, entry)

    return



  unregisterAll : (self) ->

    objectsEntries = @boundObjects[self.__uid]
    
    for entry in objectsEntries

      if entry.sender == self
        self.off(entry.target, entry.type, entry.callback)

      else
        entry.sender.off(self, entry.type, entry.callback)

    return
    
