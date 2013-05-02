### define
###

class Cluster

  constructor : (@waypoints) ->


  getLineSegment : ->

    lineSegement = ""

    for waypoint, i in @waypoints

      if i == 0
        lineSegement += "M #{waypoint.x},#{waypoint.y} "
      else
        lineSegement += "L #{waypoint.x},#{waypoint.y} "

    #connect last waypoint with first
    firstWaypoint = @waypoints[0]
    lineSegement += "L #{firstWaypoint.x},#{firstWaypoint.y}"

    return lineSegement
