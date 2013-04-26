### define
underscore : _
###

_.mixin

  removeElement : (array, element) ->

    if _.isArray(array) and (index = _.indexOf(array, element)) != -1
      array.splice(index, 1)


  removeElementAt : (array, index) ->

    array.splice(index, 1)


  pluralize : (string) ->

    string.replace(/[^s]$/, (a) -> "#{a}s") 
