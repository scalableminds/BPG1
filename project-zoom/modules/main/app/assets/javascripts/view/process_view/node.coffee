###
define
###

NODE_SIZE = 64
HALF_SIZE = 32

Node = (node) ->

  cluster = null

  getCenter : ->

    centerPosition =
      x : node.x + HALF_SIZE
      y : node.y + HALF_SIZE


  getSize : ->

    NODE_SIZE

