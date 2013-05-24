### define
underscore : _
###

class JsonPatchAccumulator

  constructor : ->

    @patches = []


  addChange : (op, path, value) =>

    path = "/#{path}"
    value = value.toObject() if value?.toObject?

    if op == "remove"
      patch = { op, path }
    else
      patch = { op, path, value }

    Object.defineProperty(patch, "__timestamp", value : Date.now())

    @patches.push(patch)

    return


  peek : ->

    @patches


  allPaths : (obj) ->

    paths = []
    
    pushKey = (key, value) ->

      paths.push(key) if key?
      if _.isArray(value)
        for arrayValue, i in value
          pushKey(i, arrayValue)

      else if _.isObject(value)
        for objKey, objValue of value
          pushKey(objKey, objValue)

      return

    pushKey(null, value)

    paths



  compact : ->

    patchGroups = _.groupBy(@patches, "path")

    newPatches = []

    for path, patchGroup of patchGroups

      added = false
      for patch in patchGroup.slice(0, -1)
        switch patch.op 
          when "add" then added = true
          when "remove" then added = false

      lastPatch = _.last(patchGroup)

      lastPatch.op = "add" if added and lastPatch.op == "replace"

      if not added or lastPatch.op != "remove"
        newPatches.push(lastPatch)

    @patches = _.sortBy(newPatches, "__timestamp")


  flush : ->

    patches = @compact()
    @patches = []
    patches


