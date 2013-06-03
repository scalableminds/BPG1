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

  constructor : (domElement, @graphModel, @artifactFinder) ->

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


  addNode : (x, y, artifactId) ->

    artifact = @artifactFinder.getArtifact(artifactId)

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

    el = []

    @nodeGroups = @nodeGroups.data(@nodes.items, (data) -> data.get("id"))

    #add new nodes
    @nodeGroups.enter()
      .append("svg:g")
      .select( (data, i)-> #hacky
        el[i] = @;
        return this
      )
      .select( (data, i) =>
        artifactId = data.get("payload").get("id")
        artifact = @artifactFinder.getArtifact(artifactId, true)

        el[i].appendChild(artifact.getImage())
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
    @paths.selectAll(".edge")
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
    @clusterPaths.selectAll(".cluster")
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
      .append("svg:path")
        .attr(
          d: "m 62.584475,-76.967196 -61.6288949,0.43865 c -3.8066336,0.0271 -10.2524701,1.00117 -11.6238301,6.36022 -1.58407,7.047528 -2.111911,38.764588 0.43877,43.644831 1.880361,3.936121 3.6264785,5.248771 5.4827005,6.360051 5.4987956,2.64158 31.1436795,2.19312 31.1436795,2.19312 L 2.271705,0.45270496 38.02073,-18.189614 l 26.537644,1.8e-4 c 3.723799,0 7.129077,-2.34182 8.334118,-7.457071 2.09811,-9.288063 2.50653,-35.338064 0.438661,-41.451351 -1.959602,-5.585379 -5.252951,-9.90844 -10.746678,-9.86934 z"
        )
        .style(
          fill: "white";
          stroke: "black";
          "stroke-width": 2;
        )

    comment
      .append("svg:text")
      .attr(
        x: 0
        y: -50
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
  moveNode : (node, position, checkForCluster = false) ->

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

      @moveNode(node, position)

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


  changeBehavior : (behavior) ->

    @currentBehavior.deactivate()
    @currentBehavior = behavior
    @currentBehavior.activate()


