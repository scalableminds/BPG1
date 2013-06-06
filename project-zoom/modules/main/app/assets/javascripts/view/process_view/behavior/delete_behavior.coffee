### define
./behavior : Behavior
app: app
d3: d3
###

class DeleteBehavior extends Behavior

  activate : (@element) ->

    element = d3.select(element)

    if element.classed("node")
      @removeNode()

    if element.classed("cluster")
      @removeCluster()


  removeNode : ->

    if window.confirm("Are you sure you want to delete this document?")
      node = d3.select(@element).datum()
      @graph.removeNode(node)

      app.trigger "behavior:delete"


  removeEdge : (event) =>

    edge = d3.select(@element).datum()
    @graph.removeEdge(edge)


    app.trigger "behavior:delete"


  removeCluster : (event) =>

    cluster = d3.select(@element).datum()
    @graph.removeCluster(cluster)

    app.trigger "behavior:delete"
