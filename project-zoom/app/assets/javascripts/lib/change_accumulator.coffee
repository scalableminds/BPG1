### define
underscore : _
###

class ChangeAccumulator

  constructor : ->

    @changes = []


  addChange : (change) =>

    change = _.object(
      _.pairs(change).map( ( [key, value] ) ->
        [key, if value?.toObject then value.toObject() else value]
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