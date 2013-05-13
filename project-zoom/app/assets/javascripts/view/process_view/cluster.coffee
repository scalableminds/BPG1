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


  finialize : ->

    #connect last waypoint with first
    firstWaypoint = @waypoints[0]
    @waypoints.push firstWaypoint
