### define
underscore : _
###

_.mixin

  removeElement : (array, element) ->

    if (index = _.indexOf(array, element)) != -1
      array.splice(index, 1)


  removeElementAt : (array, index) ->

  	array.splice(index, 1)
