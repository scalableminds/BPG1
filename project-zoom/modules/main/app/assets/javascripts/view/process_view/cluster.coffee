### define
###

Cluster = (cluster) ->

  getLineSegment : ->

    waypoints = cluster.get("waypoints")

    lineFunction = d3.svg.line(waypoints.items)
      .x( (data) -> data.get("x") )
      .y( (data) -> data.get("y") )
      .interpolate("basis") # smoothing bitches!!!

    lineFunction(waypoints.items)


  finalize : ->

    #connect last waypoint with first
    waypoints = cluster.get("waypoints")

    waypoints.add(waypoints.first().toObject())


  checkForNode : (node) ->


    #save the reference both in the cluster and in the node
    if @pointInPolygon(node)

      #if node is already associated with cluster, then dont do anything
      unless node.cluster == cluster

        #else, associate it
        node.cluster = cluster
        cluster.get("nodes").add(node)

      return true

    return false


  checkForNodes : (nodes) ->

    for node in nodes
      @checkForNode(node)


  removeNode : (node) ->

    cluster.get("nodes").remove(node)
    node.cluster = null if node.cluster == cluster


  # alogrithm uses even-odd-rule
  # http://alienryderflex.com/polygon/
  pointInPolygon : (point) ->

    waypoints = cluster.get("waypoints")

    { x, y } = point

    #calculate from the center of a node
    #x += point.getSize() / 2
    #y += point.getSize() / 2

    [ lX, lY ] = waypoints.last().pick("x", "y")
    result = false

    waypoints.each (p) ->

      [ pX, pY ] = p.pick("x", "y")

      if (
        ((pY < y and Y >= y) or
        (pY < y and pY >= y)) and
        (pX <= x or pY <= x)
      )

        if (pX + (y - pY) / (lY - pY) * (lX - pX)) < x
          result = not result

      [ lX, lY ] = [ pX, pY ]

    return result


  getCenter : ->

    waypoints = cluster.get("waypoints")

    minX = waypoints.min( (waypoint) -> waypoint.get("x") )
    maxX = waypoints.max( (waypoint) -> waypoint.get("x") )

    minY = waypoints.min( (waypoint) -> waypoint.get("y") )
    maxY = waypoints.max( (waypoint) -> waypoint.get("y") )

    return {
      x : (maxX - minX) / 2
      y : (maxY - minY) / 2
    }
