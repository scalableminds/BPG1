### define
hammer : Hammer
###

class DeleteNodeBehavior

  constructor : (@graph) ->


  activate : ->

    @hammerContext = Hammer( $("svg")[0] )
      .on("tap", ".nodeElement", @removeNode)
      .on("tap", ".edge", @removeEdge)


  deactivate : ->

    @hammerContext
      .off("tap", @removeNode)
      .off("tap", @removeEdge)


  removeNode : (event) =>

    svgContainer = $(event.target).closest("foreignObject")[0]

    node = d3.select(svgContainer).datum()
    @graph.removeNode(node)


  removeEdge : (event) =>

    edge = d3.select(event.target).datum()
    @graph.removeEdge(edge)

