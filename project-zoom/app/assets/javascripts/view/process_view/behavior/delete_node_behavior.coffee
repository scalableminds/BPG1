### define
hammer : Hammer
###

class DeleteNodeBehavior

  constructor : (@graph) ->


  activate : ->

    @hammerContext = Hammer( $("svg")[0] )
      .on("tap", ".node", @removeNode)
      .on("tap", ".edge", @removeEdge)


  deactivate : ->

    @hammerContext
      .off("tap", @removeNode)
      .off("tap", @removeEdge)


  removeNode : (event) =>

    node = d3.select(event.target).datum()
    @graph.removeNode(node)


  removeEdge : (event) =>

    edge = d3.select(event.target).datum()
    @graph.removeEdge(edge)

