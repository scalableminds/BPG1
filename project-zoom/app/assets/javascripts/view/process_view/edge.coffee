###
define
###


class Edge


  NODE_SIZE = 64
  HALF_SIZE = 32
  MARGIN = 2

  PI = Math.PI
  PI2 = 2 * Math.PI
  PI_HALF = PI / 2
  PI_QUARTER = PI / 4

  constructor : (@source, @target) ->


  getArrowDirection : ->

    if @source.x > @target.x
      return "url(#start-arrow)"
    else
      return "url(#end-arrow)"


  getLineSegment : ->

    #use center of the rectangles
    target =
      x : @target.x + HALF_SIZE
      y : @target.y + HALF_SIZE

    source =
      x : @source.x + HALF_SIZE
      y : @source.y + HALF_SIZE


    # deltaX = target.x - source.x
    # deltaY = target.y - source.y
    # dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY)
    # return if dist == 0

    # normX = deltaX / dist
    # normY = deltaY / dist

    # sourcePadding = NODE_SIZE
    # targetPadding = NODE_SIZE + 2

    # sourceX = source.x + (sourcePadding * normX)
    # sourceY = source.y + (sourcePadding * normY)
    # targetX = target.x - (targetPadding * normX)
    # targetY = target.y - (targetPadding * normY)

    # return "M#{sourceX},#{sourceY}L#{targetX},#{targetY}"

    distX = Math.abs(target.x - source.x)
    distY = Math.abs(target.y - source.y)
    hypothenuse = Math.sqrt( distY * distY + (distX * distX) )

    #quadrants & angle
    if target.x <= source.x and target.y >= source.y
      angle = Math.asin(distX / hypothenuse)

    if target.x <= source.x and target.y < source.y
      angle = PI - Math.asin(distX / hypothenuse)

    if target.x > source.x and target.y <= source.y
      angle = PI + Math.asin(distX / hypothenuse)

    if target.x > source.x and target.y > source.y
      angle = PI2 - Math.asin(distX / hypothenuse)


    #snap points
    if PI2 - PI_QUARTER < angle or angle <= PI_HALF - PI_QUARTER
      target.y -= HALF_SIZE

    if PI_HALF - PI_QUARTER < angle <= PI - PI_QUARTER
      target.x += HALF_SIZE

    if PI - PI_QUARTER < angle <= PI + PI_QUARTER
      target.y += HALF_SIZE

    if PI + PI_QUARTER < angle <= PI2 - PI_QUARTER
      target.x -= HALF_SIZE




    return "M#{source.x},#{source.y} L#{target.x},#{target.y}"

