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


  unionAll : (args...) -> _.flatten(args)


  startsWith : (haystack, needle) ->

  	if _.isString(haystack)
      haystack.substring(0, needle.length) == needle
    else
      _.isEqual(_.first(haystack, needle.length), needle)
