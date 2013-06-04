### define
hammer : Hammer
./behavior : Behavior
###

class DeleteBehavior extends Behavior

  activate : ->

    @hammerContext = Hammer( @graph.svgEl )
      .on("tap", ".node", @removeNode)
      .on("tap", ".edge", @removeEdge)
      .on("tap", ".cluster", @removeCluster)


  deactivate : ->

    @hammerContext
      .off("tap", @removeNode)
      .off("tap", @removeEdge)
      .off("tap", @removeCluster)


  removeNode : (event) =>

    node = d3.select(event.target).datum()
    @graph.removeNode(node)


  removeEdge : (event) =>

    edge = d3.select(event.target).datum()
    @graph.removeEdge(edge)


  removeCluster : (event) =>

    cluster = d3.select(event.target).datum()
    @graph.removeCluster(cluster)

