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

    @drawNodes()
    @drawEdges()
    @drawClusters()


  addNode : (x, y, artifact) ->

    node = new DataItem({ x, y })
    node.artifact = artifact

    @graphModel.get("nodes").add(node)

    #was the node dropped in a cluster?
    @graphModel.get("clusters").each (cluster) ->
      Cluster(cluster).checkForNode(node)

    @drawNodes()


  addEdge : (source, target) ->

    edge = new DataItem(
      from : source.get("id")
      to : target.get("id")
    )
    @graphModel.get("edges").add(edge)
    @drawEdges()


  addCluster : (cluster) ->

    @graphModel.get("nodes").each(Cluster(cluster).checkForNodes)
    @graphModel.get("clusters").add(cluster)
    @drawClusters()


  removeNode : (node) ->

    @graphModel.get("nodes").remove(node)

    @graphModel.get("edges")
      .select( (edge) -> edge.to == node.get("id") or edge.from == node.get("id") )
      .each( (edge) => @graphModel.get("edges").remove(edge) )

    @drawNodes()
    @drawEdges()


  removeEdge : (edge) ->

    @graphModel.get("edges").remove(edge)
    @drawEdges()


  drawNodes : ->

    nodes = @graphModel.get("nodes")

    @foreignObjects = @foreignObjects.data(nodes.items, (data) -> data.__uid)

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
                    <img class="nodeElement" draggable="false" data-id="#{data.__uid}">
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

    @paths = @paths.data(
      @graphModel.get("edges")
        .map( (edge) => new Edge(
          @graphModel.get("nodes").find( (n) -> edge.get("from") == n.get("id") ),
          @graphModel.get("nodes").find( (n) -> edge.get("to") == n.get("id") )
        )
      )
    )

    #add new edges
    @paths.enter().append("svg:path")
      .attr(
        class : "edge"
        d : (data) -> data.getLineSegment()
      )
      .style("marker-end", -> "url(#end-arrow)")

    #update existing ones
    @paths.attr(
      d : (data) -> data.getLineSegment()
    )

    #remove deleted edges
    @paths.exit().remove()


  drawClusters : ->

    @clusterPaths = @clusterPaths.data(@graphModel.get("clusters").items)

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

    node = @graphModel.get("nodes").find( (node) -> node.__uid == nodeId )

    node.set( 
      x : position.x
      y : position.y
    )

    if checkForCluster
      @graphModel.get("clusters")
        .filter( (cluster) -> not Cluster(cluster).checkForNode(node) )
        .forEach( (cluster) ->  Cluster(cluster).removeNode(node) )
       

    @drawNodes()
    @drawEdges()


  moveCluster : (clusterId, distance) ->

    cluster = @graphModel.get("clusters").find( (cluster) -> cluster.get("id") == clusterId )

    #move waypoints
    cluster.get("waypoints").each (waypoint) ->
      waypoint.set(
        x : waypoint.get("x") + distance.x
        y : waypoint.get("y") + distance.y
      )

    #move child nodes
    cluster.get("nodes").each (node) ->
      
      position =
        x : node.get("x") + distance.x
        y : node.get("y") + distance.y

      @moveNode(node.id, position)

    #actually move the svg elements
    @drawClusters()
    @drawNodes()



