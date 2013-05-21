### define
jquery : $
###


class Project

  node : null
  image : null

  constructor : (project) ->

    {@name, img:@image, @node, @tags} = project


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
    @node.x.baseVal.value = pos_x #- (@node.getSize()/2)
    @node.y.baseVal.value = pos_y #- (@node.getSize()/2)
    @node


  destroy : ->

  activate : ->

  deactivate : ->


