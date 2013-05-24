###
define
###

NODE_SIZE = 64
HALF_SIZE = 32

Node = (node) ->

  cluster = null

  getCenter : ->

    centerPosition =
      x : node.get("x") + HALF_SIZE
      y : node.get("y") + HALF_SIZE


  getSize : ->

    width : NODE_SIZE
    height : NODE_SIZE

