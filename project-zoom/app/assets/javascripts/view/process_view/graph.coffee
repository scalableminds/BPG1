### define
jquery : $
d3 : d3
./node : Node
./edge : Edge
###

class Graph

  NODE_SIZE = 20

  constructor : (@container) ->

    @nodes = []
    @edges = []

    @paths = @container.append("svg:g").selectAll("path")
    @circles = @container.append("svg:g").selectAll("circles")

    @colors = d3.scale.category10()

    @nodeId = 0


  addForeignObject : (object) ->

    foreignObject = document.createElementNS('http://www.w3.org/2000/svg', 'foreignObject' )
    body = document.createElement( 'body' )
    $(foreignObject).attr("x", 0).attr("y", 0).attr("width", 64).attr("height", 64).append(body)
    $(body).append(object)
    $("g:first").append(foreignObject)


  addNode : (x, y) =>

    tmp = new Node(x, y, @nodeId++)
    @nodes.push(tmp)

    @drawNodes(tmp)


  addEdge : (source, target) =>

    maxNodeIndex = @nodes.length - 1
    if source <= maxNodeIndex and target <= maxNodeIndex

      tmp = new Edge(@nodes[source], @nodes[target])
      @edges.push(tmp)

      @drawEdges(tmp)


  removeNode : (node) ->

    index = @nodes.indexOf(node)
    if index > -1

      @nodes.splice(index, 1)

      #remove all edges connected to the node
      for edge,i in @edges
        if edge.source == node or edge.target == nodeId
          @edges.splice(i,1)

      drawNodes()
      drawEdges()


  removeEdge : (edge) ->

    index = @edges.indexOf(edge)
    if index > -1

      @edges.splice(index, 1)
      drawEdges()


  drawNodes : (node) ->

    @circles = @circles.data(@nodes, (d) -> d.id)

    #add new nodes or update existing one
    circle = @circles.enter().append("svg:circle")
    circle
      .attr("class", "node")
      .attr("r", NODE_SIZE)
      .attr("cx", (d) -> d.x)
      .attr("cy", (d) -> d.y)
      .style("fill", (d) => @colors(d.id))
      .style("stroke", (d) => d3.rgb(@colors(d.id)).darker().toString())

    #remove deleted nodes
    @circles.exit().remove()


  drawEdges : (node) ->

    @paths = @paths.data(@edges)

    #add new edges or update existing ones
    path = @paths.enter().append("svg:path")
    path
      .attr("class", "edge")
      .attr("d", (data) -> data.getLineSegment())
      .style("marker-end", (d) -> "url(#end-arrow)")

    #remove delte edges
    @paths.exit().remove()



