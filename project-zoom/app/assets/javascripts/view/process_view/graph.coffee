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

      @drawEdges(tmp)


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

    index = @edges.indexOf(edge) - 1
    if index > -1

      @edges.splice(index, 1)
      @drawEdges()


  drawNodes : (node) ->

    HTML = ""
    @circles = @circles.data(@nodes, (d) -> d.id)

    #add new nodes or update existing one
    circle = @circles.enter()
      .append("svg:foreignObject")
        .attr("class", "node")
        #.attr("r", NODE_SIZE)
        .attr("x", (d) -> d.x)
        .attr("y", (d) -> d.y)
        .attr("width", 68)
        .attr("height", 68)
      .append("xhtml:div")
        .attr("workaround", (d, i) ->
          if d.artifact?
            HTML = d.artifact.domElement
          else
            HTML = """<div class="nodeElement" style="background-color:#{d3.scale.category10()(d.id)}">""" #return HTML element

          $(this).append(HTML)
          return ""
        )

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



