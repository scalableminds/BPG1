### define
jquery : $
d3 : d3
./node : Node
./edge : Edge
./cluster : Cluster
lib/data_item : DataItem
###

class Graph

  constructor : (@container, @graphModel) ->

    @clusterPaths = @container.append("svg:g").selectAll("path")
    @paths = @container.append("svg:g").selectAll("path")
    @nodeGroups = @container.append("svg:g").selectAll("images")

    @colors = d3.scale.category10()

    @nodes = @graphModel.get("nodes")
    @edges = @graphModel.get("edges")
    @clusters = @graphModel.get("clusters")

    @drawNodes()
    @drawEdges()
    @drawClusters()


  addNode : (x, y, artifact) ->

    node = new DataItem(
      position : { x, y }
      id : @nextId()
      payload :
        id : "519b693d030655c8752c2973"
      typ : "project"
    )
    #node.set({artifact})
    node.artifact = artifact

    @nodes.add(node)

    #was the node dropped in a cluster?
    @graphModel.get("clusters").each (cluster) ->
      Cluster(cluster).ensureNode(node)

    @drawNodes()


  addEdge : (source, target) ->

    edge = new DataItem(
      from : source.get("id")
      to : target.get("id")
    )
    @edges.add(edge)
    @drawEdges()


  addCluster : (cluster) ->

    Cluster(cluster).ensureNodes(@nodes)
    @clusters.add(cluster)
    @drawClusters()


  removeNode : (node) ->

    @nodes.remove(node)

    @edges
      .filter( (edge) -> edge.get("to") == node.get("id") or edge.get("from") == node.get("id") )
      .forEach( (edge) => @edges.remove(edge) )

    @clusters
      .filter( (cluster) -> cluster.get("content").contains(node.get("id")) )
      .forEach( (cluster) -> Cluster(cluster).removeNode(node) )


    @drawNodes()
    @drawEdges()


  removeEdge : (edge) ->

    @edges.remove(edge)
    @drawEdges()


  removeCluster : (cluster) ->

    Cluster(cluster).getNodes(@nodes).forEach (node) =>
      Cluster(cluster).removeNode(node)

    @clusters.remove(cluster)

    @drawClusters()
    @drawNodes()


  drawNodes : ->

    @nodeGroups = @nodeGroups.data(@nodes.items, (data) -> data.get("id"))
    imageElement = "svg:image"

    #add new nodes
    @nodeGroups.enter()
      .append("svg:g")
        #.attr("workaround", (data) -> artifact = data.artifact; if artifact? then imageElement = artifact.getImage())
      .append("svg:image")
      .attr(

        class : "node"

        x : (data) -> data.get("position/x") - Node(data).getSize().width / 2
        y : (data) -> data.get("position/y") - Node(data).getSize().height / 2

        width : (data) -> Node(data).getSize().width
        height : (data) -> Node(data).getSize().height

        "xlink:href" : "assets/images/thumbnails/thumbnail/1.png"
        "data-id": (data) -> data.get("id")
      )


    @drawComment(@nodeGroups, Node)

    #update existing ones
    @nodeGroups.selectAll("image").attr(
      x : (data) -> data.get("position/x") - Node(data).getSize().width / 2
      y : (data) -> data.get("position/y") - Node(data).getSize().height / 2
    )

    #remove deleted nodes
    @nodeGroups.exit().remove()


  drawEdges : ->

    @paths = @paths.data(@edges.items, (data) -> data.__uid)

    #add new edges
    @paths.enter()
      .append("svg:g")
      .append("svg:path")
        .attr(
          class : "edge"
          d : (data) => Edge(data, @nodes).getLineSegment()
        )
        .style("marker-end", "url(#end-arrow)")

    @drawComment(@paths, Edge)

    #update existing ones
    @paths.selectAll("path")
      .attr(
        d : (data) => Edge(data, @nodes).getLineSegment()
      )

    #remove deleted edges
    @paths.exit().remove()


  drawClusters : ->

    @clusterPaths = @clusterPaths.data(@clusters.items, (data) -> data.get("id"))

    #add new edges or update existing ones
    @clusterPaths.enter()
      .append("svg:g")
      .append("svg:path")
        .attr(
          class : "cluster"
          "data-id" : (data) -> data.get("id")
          d : (data) -> Cluster(data).getLineSegment()
        )

    @drawComment(@clusterPaths, Cluster)

    #update existing ones
    @clusterPaths.select("path")
      .attr(
        d : (data) -> Cluster(data).getLineSegment()
      )

    #remove deleted edges
    @clusterPaths.exit().remove()


  drawComment : (element, elementType) ->

    commentGroup = element.selectAll("g").data(
      (data) ->
        if data.get("comment") then [data] else []
      ,(data) ->
        data.get("id"))

    comment = commentGroup.enter()
      .append("g")
        .attr(
          transform: (data) =>
            unless elementType == Edge
              position = elementType(data).getCommentPosition()
            else
              position = elementType(data, @nodes).getCommentPosition()

            "translate(#{position.x}, #{position.y})"
        )

    comment
      .append("svg:use")
      .attr(
        x: -40
        y: -120
        "xlink:href": "#comment-callout"
      )

    comment
      .append("svg:text")
      .attr(
        x: -20
        y: -70
        width: 80
        height: 40
      )
      .text( (data) -> data.get("comment"))

    #update existing ones
    commentGroup
      .attr(
        transform: (data) =>
            unless elementType == Edge
              position = elementType(data).getCommentPosition()
            else
              position = elementType(data, @nodes).getCommentPosition()

            "translate(#{position.x}, #{position.y})"
      )

    commentGroup.selectAll("text")
      .text( (data) ->  data.get("comment") )

    #remove deleted comments
    commentGroup.exit().remove()


  # position.x/y are absolute positions
  moveNode : (nodeId, position, checkForCluster = false) ->

    node = @nodes.find( (node) -> node.get("id") == nodeId )

    node.set({ position })

    if checkForCluster
      @clusters
        .filter( (cluster) -> not Cluster(cluster).ensureNode(node) )
        .forEach( (cluster) ->  Cluster(cluster).removeNode(node) )

    @drawNodes()
    @drawEdges()


  moveCluster : (clusterId, distance) ->

    cluster = @clusters.find( (cluster) -> cluster.get("id") == clusterId )

    #move waypoints
    cluster.update("waypoints", (waypoints) ->

      waypoints.toObject().map( (waypoint) ->
        x : waypoint.x + distance.x
        y : waypoint.y + distance.y
      )

    )

    #move child nodes
    Cluster(cluster).getNodes(@nodes).forEach (node) =>

      position =
        x : node.get("position/x") + distance.x
        y : node.get("position/y") + distance.y

      @moveNode(node.get("id"), position)

    #actually move the svg elements
    @drawClusters()
    @drawNodes()


  nextId : ->

    _.max(
      _.flatten [
        @nodes.pluck("id")
        @clusters.pluck("id")
        [0]
      ]
    ) + 1




