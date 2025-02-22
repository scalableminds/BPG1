### define
underscore : _
###

Cluster = (cluster) ->

  getLineSegment : (graph) ->

    waypoints = cluster.get("waypoints")

    lineFunction = d3.svg.line(waypoints.items)
      .x( (data) -> data.get("x"))
      .y( (data) -> data.get("y"))
      .interpolate("basis") # smoothing bitches!!!

    lineFunction(waypoints.items)


  finalize : ->

    #connect last waypoint with first
    waypoints = cluster.get("waypoints")

    waypoints.add(waypoints.first().toObject())


  ensureNode : (node) ->

    #save the reference both in the cluster and in the node
    if @pointInPolygon(node)

      #if node is already associated with a cluster, then dont do anything
      unless cluster.get("content").contains(node.get("id"))
        cluster.get("content").add(node.get("id"))

      return true

    return false


  ensureNodes : (nodes) ->

    nodes.forEach (node) =>
      @ensureNode(node); return


  getNodes : (nodeList) ->

    nodeList.filter( (node) -> cluster.get("content").contains(node.get("id")) )


  removeNode : (node) ->

    cluster.get("content").remove(node.get("id"))


  # alogrithm uses even-odd-rule
  # http://alienryderflex.com/polygon/
  pointInPolygon : (node) ->

    waypoints = cluster.get("waypoints")

    { x, y } = node.get("position").pick("x", "y")

    { x : lX, y : lY } = waypoints.last().pick("x", "y")

    waypoints.reduce(

      (result, p) ->

        { x : pX, y : pY } = p.pick("x", "y")

        if (
          ((pY < y and lY >= y) or
          (lY < y and pY >= y)) and
          (pX <= x or lY <= x)
        ) and (pX + (y - pY) / (lY - pY) * (lX - pX)) < x
          result = not result

        [ lX, lY ] = [ pX, pY ]
        result

      false
    )


  getCenter : ->

    waypoints = cluster.get("waypoints")

    minX = waypoints.min( (waypoint) -> waypoint.get("x") )
    maxX = waypoints.max( (waypoint) -> waypoint.get("x") )

    minY = waypoints.min( (waypoint) -> waypoint.get("y") )
    maxY = waypoints.max( (waypoint) -> waypoint.get("y") )

    return {
      x : (maxX.get("x") - minX.get("x")) / 2
      y : (maxY.get("y") - minY.get("y")) / 2
    }


  getCommentPosition : ->

    waypoints = cluster.get("waypoints")

    upperRightCorner =
      x: waypoints.max( (waypoint) -> waypoint.get("x") ).get("x")
      y: waypoints.min( (waypoint) -> waypoint.get("y") ).get("y")

    distance = Math.abs(upperRightCorner.x) + Math.abs(upperRightCorner.y)
    position = null

    #manhattan distance
    waypoints.forEach (waypoint) ->

      distX = Math.abs(upperRightCorner.x - waypoint.get("x"))
      distY = Math.abs(upperRightCorner.y - waypoint.get("y"))

      manhattanDistance = distX + distY
      if manhattanDistance < distance
        distance = manhattanDistance
        position = waypoint

    result =
      x: position?.get("x") or upperRightCorner.x
      y: position?.get("y") or upperRightCorner. y


  getColor : ->

    colorMap =
      "freeform" : "#000000"
      "Understand" : "#388aac"
      "Observe" : "#b72772"
      "POV" : "#4aa751"
      "Ideate" : "#e25c36"
      "Prototype" : "#d33924"
      "Test" : "#a52025"

    clusterType = cluster.get("phase")

    colorMap[clusterType]
