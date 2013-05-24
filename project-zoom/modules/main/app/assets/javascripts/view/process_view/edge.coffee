###
define
./node : Node
###

Edge = (edge, nodeList) ->

  PI = Math.PI
  PI2 = 2 * Math.PI
  PI_HALF = PI / 2
  PI_QUARTER = PI / 4

  comment : null
  source : nodeList.find( (node) -> node.get("id") == edge.get("from"))
  target : nodeList.find( (node) -> node.get("id") == edge.get("to"))

  getArrowDirection : ->

    if @source.get("x") > @target.get("x")
      return "url(#start-arrow)"
    else
      return "url(#end-arrow)"


  getLineSegment : ->

    target = @target
    source = @source

    @HALF_SIZE = Node(@target).getSize().width / 2

    targetSourceAngle = @calcAngle(target, source)
    snapPoint = @getSnapPoint(targetSourceAngle, target.pick("x", "y"))

    # sourceTargetAngle = @calcAngle(source, target)
    # source = @getSnapPoint(sourceTargetAngle, source)

    return "M#{source.get("x")},#{source.get("y")} L#{snapPoint.x},#{snapPoint.y}"


  calcAngle : (target, source) ->

    tX = target.get("x"); tY = target.get("y")
    sX = source.get("x"); sY = source.get("y")

    distX = Math.abs(tX - sX)
    distY = Math.abs(tY - sY)
    hypothenuse = Math.sqrt( distY * distY + (distX * distX) )

    # quadrants & angle
    if tX <= sX and tY >= sY
      angle = Math.asin(distX / hypothenuse)

    else if tX <= source.get("x") and tY < sY
      angle = PI - Math.asin(distX / hypothenuse)

    else if tX > sX and tY <= sY
      angle = PI + Math.asin(distX / hypothenuse)

    else if tX > sX and tY > sY
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

