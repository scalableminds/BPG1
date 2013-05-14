### define
###

class Cluster

  constructor : ->

    @waypoints = []


  getLineSegment : ->

    lineSegement = ""

    for waypoint, i in @waypoints

      if i == 0
        lineSegement += "M #{waypoint.x},#{waypoint.y} "
      else
        lineSegement += "L #{waypoint.x},#{waypoint.y} "

    return lineSegement


  finialize : (nodes) ->

    #connect last waypoint with first
    firstWaypoint = @waypoints[0]
    @waypoints.push firstWaypoint


    for node in nodes

      if @pointInPolygon(node)
        node.cluster = @


  # alogrithm uses even-odd-rule
  # http://alienryderflex.com/polygon/
  pointInPolygon : (point) ->

    {x, y} = point

    #calculate from the center of a node
    x += point.getSize() / 2
    y += point.getSize() / 2

    j = _.last(@waypoints)
    result = false

    for i in @waypoints

      if (
        ((i.y < y and j.y >= y) or
        (j.y < y and i.y >= y)) and
        (i.x <= x or j.y <= x)
      )

        if (i.x + (y - i.y) / (j.y - i.y) * (j.x - i.x)) < x
          result = not result

      j = i

    return result
