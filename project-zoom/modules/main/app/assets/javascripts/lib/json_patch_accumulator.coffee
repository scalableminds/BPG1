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

    newPatches = []

    @patches.forEach (patch, i) =>

      return if patch.overridden

      patchPath = patch.path.split("/").slice(1)

      for patch2 in @patches.slice(i + 1)

        patchPath2 = patch2.path.split("/").slice(1)

        if patch.path == patch2.path and (patch2.op == "add" or patch2.op == "replace") and (patch.op == "add" or patch.op == "replace")
          patch = _.extend({}, patch, value : patch2.value)
          Object.defineProperty(patch, "__timestamp", value : patch2.__timestamp)
          patch2.overridden = true

        if patch2.op == "remove" and _.startsWith(patchPath, patchPath2)
          patch2.overridden = true
          return

        if (patch.op == "add" or patch.op == "replace") and _.startsWith(patchPath2, patchPath)
          
          remainingPath = patchPath2.slice(patchPath.length)
          obj = patch.value
          for key in remainingPath.slice(0, -1)
            obj = obj[key]
          key = _.last(remainingPath)

          if patch2.op == "add" or patch2.op == "replace"
            if _.isArray(obj) and patch2.op == "add"
              obj.splice(key, 0, patch2.value)
            else
              obj[key] = patch2.value
            remainingPath

          if patch2.op == "remove"
            if _.isArray(obj)
              obj.splice(key, 1)
            else
              delete obj[key]

          patch2.overridden = true


      newPatches.push(patch)

    @patches = _.sortBy(newPatches, "__timestamp")




  flush : ->

    patches = @compact()
    @patches = []
    patches


