###
define
###

NODE_SIZE = 64
HALF_SIZE = 32

Node = (node) ->

  cluster = null

  getCenter : ->

    centerPosition =
      x : node.get("position/x") + HALF_SIZE
      y : node.get("position/y") + HALF_SIZE


  getSize : ->

    width : NODE_SIZE
    height : NODE_SIZE


  getCommentPosition : ->

    position =
      x: node.get("position/x") + HALF_SIZE / 3
      y: node.get("position/y") - HALF_SIZE

