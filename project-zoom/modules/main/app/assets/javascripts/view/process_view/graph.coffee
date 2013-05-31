### define
jquery : $
d3 : d3
./node : Node
./edge : Edge
./cluster : Cluster
../../component/artifact : Artifact
lib/data_item : DataItem
lib/event_mixin : EventMixin
./behavior/drag_behavior : DragBehavior
###

class Graph

  constructor : (domElement, @graphModel) ->

    EventMixin.extend(this)

    @d3Element = d3.select(domElement).select("svg")
    @graphContainer = @d3Element.append("svg:g")

    @initArrowMarkers()
    @initCallouts()

    @currentBehavior = new DragBehavior(@)
    @currentBehavior.activate()

    @clusterPaths = @graphContainer.append("svg:g").selectAll("path")
    @paths = @graphContainer.append("svg:g").selectAll("path")
    @nodeGroups = @graphContainer.append("svg:g").selectAll("images")

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
      payload : artifact.dataItem
      typ : "artifact"
    )

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

    cluster.set("id", @nextId(), silent : true)
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

    #add new nodes
    @nodeGroups.enter()
      .append("svg:g")
      .append("svg:image")
        .attr(
          class : "node"

          x : (data) -> data.get("position/x") - Node(data).getSize().width / 2
          y : (data) -> data.get("position/y") - Node(data).getSize().height / 2

          width : (data) -> Node(data).getSize().width
          height : (data) -> Node(data).getSize().height

          "xlink:href" : (data) -> new Artifact(data.get("payload"), (->64), true, this).resize()
          "data-id": (data) -> data.get("id")
        )

    @drawComment(@nodeGroups, Node)

    #update existing ones
    @nodeGroups.selectAll("image").attr(
      x : (data) -> data.get("position/x") - Node(data).getSize().width / 2
      y : (data) -> data.get("position/y") - Node(data).getSize().height / 2
      #"xlink:href" : (data) -> a = new Artifact(data.get("payload"), (->64), true, this); a.activate(); a.resize()
      "xlink:href" : (data) -> new Artifact(data.get("payload"), (->64), true, this).resize()
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


  initArrowMarkers : ->

    # define arrow markers for graph edges
    @d3Element.append("svg:defs")
      .append("svg:marker")
        .attr("id", "end-arrow")
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 6)
        .attr("markerWidth", 3)
        .attr("markerHeight", 3)
        .attr("orient", "auto")
      .append("svg:path")
        .attr("d", "M0,-5L10,0L0,5")
        .attr("fill", "#000")

    @d3Element.append("svg:defs")
      .append("svg:marker")
        .attr("id", "start-arrow")
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 4)
        .attr("markerWidth", 3)
        .attr("markerHeight", 3)
        .attr("orient", "auto")
      .append("svg:path")
        .attr("d", "M10,-5L0,0L10,5")
        .attr("fill", "#000")


  initCallouts : ->

    @d3Element.append("svg:defs")
      .append("svg:g")
        .attr(
          id: "comment-callout"
          class: "comment-callout"
        )
        .append("svg:path")
          .attr(
            d: "M 91.327094,24.650308 29.698196,25.08895 c -3.806634,0.02708 -10.252469,1.001177 -11.623829,6.360221 -1.58407,7.047529 -2.111911,38.764592 0.43877,43.644832 1.880362,3.93612 3.626479,5.248771 5.482699,6.360056 5.498796,2.64158 31.143682,2.193113 31.143682,2.193113 L 31.014321,102.0702 66.763348,83.427882 l 26.537645,1.8e-4 c 3.723799,0 7.129077,-2.341813 8.334117,-7.45707 2.09811,-9.288053 2.50653,-35.338056 0.43866,-41.451347 -1.9596,-5.58538 -5.252949,-9.90844 -10.746676,-9.869337 z"
          )
          .style(
            fill: "white";
            stroke: "black";
            "stroke-width": 2;
          )

  changeBehavior : (behavior) ->

    @currentBehavior.deactivate()
    @currentBehavior = behavior
    @currentBehavior.activate()


