### define
jquery : $
###


class Tagbar

  TAB_PREFIX : "tab"

  domElement : null
  artifacts : null


  constructor : () ->
    @tags = []

    domElement = $('<div/>', {})

    project = @SAMPLE_PROJECT
    @domElement = domElement


  setResized : (func) ->
    @onResized = func


  arrangeProjectGraph : () ->


  destroy : ->

  activate : ->

  deactivate : ->