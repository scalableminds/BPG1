### define
jquery : $
d3 : d3
./node : Node
./edge : Edge
./cluster : Cluster
###

class Graph

  NODE_SIZE = 20

  constructor : (@container) ->

    @nodes = []
    @edges = []
    @clusters = []

    @clusterPaths = @container.append("svg:g").selectAll("path")
    @paths = @container.append("svg:g").selectAll("path")
    @foreignObjects = @container.append("svg:g").selectAll("foreignObject")

    @colors = d3.scale.category10()

    @nodeId = 0


  addForeignObject : (object) ->

    foreignObject = document.createElementNS('http://www.w3.org/2000/svg', 'foreignObject' )
    $(foreignObject).attr("x", 0).attr("y", 0).attr("width", 64).attr("height", 64).append(object)
    $("g:first").append(foreignObject)


  addNode : (x, y, artifact) =>

    tmp = new Node(x, y, @nodeId++, artifact)
    @nodes.push(tmp)

    @drawNodes(tmp)


  addEdge : (source, target) =>

    maxNode = @nodes[@nodes.length - 1]
    if source <= maxNode.id and target <= maxNode.id

      for node in @nodes
        sourceNode = node if node.id == source
        targetNode = node if node.id == target


      tmp = new Edge(sourceNode, targetNode)
      @edges.push(tmp)

      @drawEdges()


  addCluster : (cluster) =>

    cluster.finialize(@nodes)
    @clusters.push( cluster )
    @drawClusters()


  removeNode : (node) ->

    index = @nodes.indexOf(node)
    if index > -1

      @nodes.splice(index, 1)

      #remove all edges connected to the node
      # for edge,i in @edges
      #   if edge.source == node or edge.target == node
      #     @edges.splice(i,1)

      @drawNodes()
      @drawEdges()


  removeEdge : (edge) ->

    index = @edges.indexOf(edge)
    if index > -1

      @edges.splice(index, 1)
      @drawEdges()


  drawNodes : ->

    HTML = ""
    @foreignObjects = @foreignObjects.data(@nodes, (d) -> d.id)

    #add new nodes or update existing one
    foreignObject = @foreignObjects.enter()
      .append("svg:g")
        .attr("class", "node")
      .append("svg:foreignObject")
        .attr("x", (d) -> d.x)
        .attr("y", (d) -> d.y)
        .attr("width", 68)
        .attr("height", 68)
        .attr("workaround", (d, i) ->
          if d.artifact?
            HTML = d.artifact.domElement
          else
            HTML = """<body xmlns="http://www.w3.org/1999/xhtml"><div class="nodeElement" data-id="#{d.id}" style="background-color:#{d3.scale.category10()(d.id)}"></body></html>""" #return HTML element

          $(this).append(HTML)
          return ""
        )

    #remove deleted nodes
    @foreignObjects.exit().remove()


  drawEdges : ->

    @paths = @paths.data(@edges)

    #add new edges or update existing ones
    path = @paths.enter().append("svg:path")
    path
      .attr("class", "edge")
      .attr("d", (data) -> data.getLineSegment())
      .style("marker-end", (d) -> "url(#end-arrow)")

    #remove deleted edges
    @paths.exit().remove()


  drawClusters : ->

    @clusterPaths = @clusterPaths.data(@clusters)

    #add new edges or update existing ones
    clusterPath = @clusterPaths.enter().append("svg:path")
    clusterPath
      .attr("class", "cluster")
      .attr("d", (data) -> data.getLineSegment())

    #remove deleted edges
    @clusterPaths.exit().remove()





