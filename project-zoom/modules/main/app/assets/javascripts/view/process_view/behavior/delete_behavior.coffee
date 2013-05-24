### define
hammer : Hammer
./behavior : Behavior
###

class DeleteBehavior extends Behavior

  activate : ->

    @hammerContext = Hammer( $("svg")[0] )
      .on("tap", ".node-image", @removeNode)
      .on("tap", ".edge", @removeEdge)
      .on("tap", ".cluster", @removeCluster)


  deactivate : ->

    @hammerContext
      .off("tap", @removeNode)
      .off("tap", @removeEdge)
      .off("tap", @removeCluster)


  removeNode : (event) =>

    svgContainer = $(event.target).closest("foreignObject")[0]

    node = d3.select(svgContainer).datum()
    @graph.removeNode(node)


  removeEdge : (event) =>

    edge = d3.select(event.target).datum()
    @graph.removeEdge(edge)


  removeCluster : (event) =>

    cluster = d3.select(event.target).datum()
    @graph.removeCluster(cluster)

