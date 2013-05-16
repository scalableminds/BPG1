###
define
###


class Node

  NODE_SIZE = 64
  HALF_SIZE = 32

  constructor : (@x, @y, @id, @artifact = null) ->

    cluster = null


  getCenter : ->

    centerPosition =
      x : @x + HALF_SIZE
      y : @y + HALF_SIZE


  getSize : ->

    NODE_SIZE

