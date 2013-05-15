###
define
###


class Edge

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
    target = @target#.getCenter()
    source = @source#.getCenter()

    @HALF_SIZE = @target.getSize() / 2

    targetSourceAngle = @calcAngle(target, source)
    target = @getSnapPoint(targetSourceAngle, target)

    # sourceTargetAngle = @calcAngle(source, target)
    # source = @getSnapPoint(sourceTargetAngle, source)

    return "M#{source.x},#{source.y} L#{target.x},#{target.y}"


  calcAngle : (target, source) ->

    distX = Math.abs(target.x - source.x)
    distY = Math.abs(target.y - source.y)
    hypothenuse = Math.sqrt( distY * distY + (distX * distX) )

    #quadrants & angle
    if target.x <= source.x and target.y >= source.y
      angle = Math.asin(distX / hypothenuse)

    else if target.x <= source.x and target.y < source.y
      angle = PI - Math.asin(distX / hypothenuse)

    else if target.x > source.x and target.y <= source.y
      angle = PI + Math.asin(distX / hypothenuse)

    else if target.x > source.x and target.y > source.y
      angle = PI2 - Math.asin(distX / hypothenuse)

    return angle


  getSnapPoint : (angle, point) ->

    # dont change the referenced original here
    # rather return a copy
    point = _.clone(point)

    if PI2 - PI_QUARTER < angle or angle <= PI_HALF - PI_QUARTER
      point.y -= @HALF_SIZE

    else if PI_HALF - PI_QUARTER < angle <= PI - PI_QUARTER
      point.x += @HALF_SIZE

    else if PI - PI_QUARTER < angle <= PI + PI_QUARTER
      point.y += @HALF_SIZE

    else if PI + PI_QUARTER < angle <= PI2 - PI_QUARTER
      point.x -= @HALF_SIZE

    return point

