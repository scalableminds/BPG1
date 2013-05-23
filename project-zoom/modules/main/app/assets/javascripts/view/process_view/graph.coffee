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
    @foreignObjects = @container.append("svg:g").selectAll("foreignObject")

    @colors = d3.scale.category10()

    @nodes = @graphModel.get("nodes")
    @edges = @graphModel.get("edges")
    @clusters = @graphModel.get("clusters")

    @drawNodes()
    @drawEdges()
    @drawClusters()


  addNode : (x, y, artifact) ->

    node = new DataItem({ x, y })
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

    @graphModel.get("edges")
      .select( (edge) -> edge.to == node.get("id") or edge.from == node.get("id") )
      .forEach( (edge) => @edges.remove(edge) )

    @drawNodes()
    @drawEdges()


  removeEdge : (edge) ->

    @edges.remove(edge)
    @drawEdges()


  drawNodes : ->

    @foreignObjects = @foreignObjects.data(@nodes.items, (data) -> data.get("id"))

    #add new nodes
    foreignObject = @foreignObjects.enter()
      .append("svg:foreignObject")
      .attr( class : "node" )
      .attr(

        x : (data) -> data.get("x") - Node(data).getSize().width / 2
        y : (data) -> data.get("y") - Node(data).getSize().height / 2

        width : (data) -> Node(data).getSize().width
        height : (data) -> Node(data).getSize().height

        workaround : (data, i) ->

          if data.artifact?
            html = data.artifact.domElement
          else
            html = """
              <html xmlns="http://www.w3.org/1999/xhtml">
                <body>
                  <div class="nodeElement" style="background-color: rgb(0,127,255)">
                    <img class="nodeElement" draggable="false" data-id="#{data.get("id")}">
                </body>
              </html>""" #return HTML element

          $(this).append(html)
          return ""
      )

    #update existing ones
    @foreignObjects.attr(
      x : (data) -> data.get("x") - Node(data).getSize().width / 2
      y : (data) -> data.get("y") - Node(data).getSize().height / 2
    )

    #remove deleted nodes
    @foreignObjects.exit().remove()


  drawEdges : ->

    @paths = @paths.data(@edges.items, (data) -> data.__uid)

    #add new edges
    @paths.enter().append("svg:path")
      .attr(
        class : "edge"
        d : (data) => Edge(data, @nodes).getLineSegment()
      )
      .style("marker-end", -> "url(#end-arrow)")

    #update existing ones
    @paths.attr(
      d : (data) => Edge(data, @nodes).getLineSegment()
    )

    #remove deleted edges
    @paths.exit().remove()


  drawClusters : ->

    @clusterPaths = @clusterPaths.data(@clusters.items, (data) -> data.get("id"))

    #add new edges or update existing ones
    clusterPath = @clusterPaths.enter().append("svg:path")
    clusterPath
      .attr(
        class : "cluster"
        "data-id" : (data) -> data.get("id")
        d : (data) -> Cluster(data).getLineSegment()
      )

    #update existing ones
    @clusterPaths.attr(
      d : (data) -> Cluster(data).getLineSegment()
    )

    #remove deleted edges
    @clusterPaths.exit().remove()


  # position.x/y are absolute positions
  moveNode : (nodeId, position, checkForCluster = false) ->

    node = @nodes.find( (node) -> node.get("id") == nodeId )

    node.set( 
      x : position.x
      y : position.y
    )

    if checkForCluster
      @clusters
        .filter( (cluster) -> not Cluster(cluster).ensureNode(node) )
        .forEach( (cluster) ->  Cluster(cluster).removeNode(node) )
       

    @drawNodes()
    @drawEdges()


  moveCluster : (clusterId, distance) ->

    cluster = @clusters.find( (cluster) -> cluster.get("id") == clusterId )

    #move waypoints
    cluster.get("waypoints").each (waypoint) ->
      waypoint.set(
        x : waypoint.get("x") + distance.x
        y : waypoint.get("y") + distance.y
      )

    #move child nodes
    Cluster(cluster).getNodes(@nodes).forEach (node) =>
      
      position =
        x : node.get("x") + distance.x
        y : node.get("y") + distance.y

      @moveNode(node.get("id"), position)

    #actually move the svg elements
    @drawClusters()
    @drawNodes()



