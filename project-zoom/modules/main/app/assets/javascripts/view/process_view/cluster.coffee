### define
###

class Cluster

  constructor : ->

    @waypoints = []
    @nodes = []

    @id = 0
    @comment = null


  getLineSegment : ->

    lineFunction = d3.svg.line(@waypoints)
      .x( (data) -> data.x )
      .y( (data) -> data.y )
      .interpolate("basis") # smoothing bitches!!!

    lineFunction(@waypoints)


  finalize : ->

    #connect last waypoint with first
    firstWaypoint = _.deepClone(@waypoints[0])
    @waypoints.push firstWaypoint


  checkForNode : (node) ->


    #save the reference both in the cluster and in the node
    if @pointInPolygon(node)

      #if node is already associated with cluster, then dont do anything
      unless node.cluster == @

        #else, associate it
        node.cluster = @
        @nodes.push node

      return true

    return false


  checkForNodes : (nodes) ->

    for node in nodes
      @checkForNode(node)


  removeNode : (node) ->

    index = @nodes.indexOf(node)

    if index > -1
      @nodes.splice(index, 1)
      node.cluster = null


  # alogrithm uses even-odd-rule
  # http://alienryderflex.com/polygon/
  pointInPolygon : (point) ->

    {x, y} = point

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


  getCenter : ->

    minX = _.min(waypoints, (waypoint) -> waypoint.x)
    maxX = _.mx(waypoints, (waypoint) -> waypoint.x)

    minY = _.min(waypoints, (waypoint) -> waypoint.y)
    maxY = _.max(waypoints, (waypoint) -> waypoint.y)

    return {
      x : (maxX - minX) / 2
      y : (maxY - minY) / 2
    }
