###
define
###


class Edge


  NODE_SIZE = 20

  constructor : (@source, @target) ->


  getArrowDirection : ->

    if @source.x > @target.x
      return "url(#start-arrow)"
    else
      return "url(#end-arrow)"


  getLineSegment : ->

    { source, target } = this

    deltaX = target.x - source.x
    deltaY = target.y - source.y
    dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY)
    return if dist == 0

    normX = deltaX / dist
    normY = deltaY / dist

    sourcePadding = NODE_SIZE
    targetPadding = NODE_SIZE + 2

    sourceX = source.x + (sourcePadding * normX)
    sourceY = source.y + (sourcePadding * normY)
    targetX = target.x - (targetPadding * normX)
    targetY = target.y - (targetPadding * normY)

    return "M#{sourceX},#{sourceY}L#{targetX},#{targetY}"


