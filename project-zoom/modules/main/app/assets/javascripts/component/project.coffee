### define
jquery : $
###


class Project

  node : null
  image : null

  constructor : (name) ->
    @name = name
    @image = null
    @node = null
    @tags = []

  setNode : (node) ->
    @node = node

  node : ->
    @node

  addTag : (tag) ->
    @tags.push tag

  tags : () ->
    @tags

  setImage : (img_src) ->
    @image = img_src

  image : () ->
    @image

  moveNode : (pos_x, pos_y) ->
    @node.x.baseVal.value = pos_x
    @node.y.baseVal.value = pos_y
    @node


  destroy : ->

  activate : ->

  deactivate : ->


