### define
underscore : _
###

Cluster = (cluster) ->

  comment = null

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


  ensureNode : (node) ->

    #save the reference both in the cluster and in the node
    if @pointInPolygon(node)

      #if node is already associated with a cluster, then dont do anything
      unless node.cluster == cluster

        #else, associate it
        node.cluster = cluster
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
    node.cluster = null if node.cluster == cluster


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
      x : (maxX - minX) / 2
      y : (maxY - minY) / 2
    }
