### define
./behavior : Behavior
###

class CommentBehavior extends Behavior

  activate : ->

    @hammerContext = Hammer( $("svg")[0])
      .on("tap", ".node-image", @commentNode )
      .on("tap", ".cluster", @commentCluster )
      .on("tap", ".edge", @commentEdge )


  deactivate : ->

    @hammerContext
      .off("tap", @commentNode)
      .off("tap", @commentCluster)
      .off("tap", @commentEdge)


  commentNode : (event) =>

    svgContainer = $(event.gesture.target).closest("foreignObject")[0]
    node = d3.select(svgContainer).datum()

    text = node.text ? ""
    @showModal text, (text) =>

      node.set(comment: text)
      @graph.drawNodes()


  commentCluster : (event) =>

    cluster = $(event.gesture.target).datum()

    text = cluster.comment ? ""
    @showModal "cluster", (text) =>

      cluster.set(comment: text)
      @graph.drawClusters()


  commentEdge : (event) =>

    edge = $(event.gesture.target).datum()

    text = edge.comment ? ""
    @showModal "cluster", (text) =>

      edge.set(comment: text)
      @graph.drawEdges()


  showModal : (text, callback) ->

    $modal = $("#comment-modal")

    $textarea = $modal.find("textarea")
    $textarea.text(text)

    $modal.on "shown", -> $textarea.focus()

    $modal.find(".btn-primary").on "click", (event) ->

      $modal.modal("hide")

      text = $textarea.val()
      callback(text)

    $modal.modal("show")




