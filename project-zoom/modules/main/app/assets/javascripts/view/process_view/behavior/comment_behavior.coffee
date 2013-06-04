### define
./behavior : Behavior
###

class CommentBehavior extends Behavior

  activate : ->

    @hammerContext = Hammer( @graph.svgEl )
      .on("tap", ".node", @commentNode )
      .on("tap", ".cluster", @commentCluster )
      .on("tap", ".edge", @commentEdge )


  deactivate : ->

    @hammerContext
      .off("tap", @commentNode)
      .off("tap", @commentCluster)
      .off("tap", @commentEdge)


  commentNode : (event) =>

    node = d3.select(event.gesture.target).datum()

    text = node.get("comment") ? ""
    @showModal text, (text) =>

      node.set(comment: text)
      @graph.drawNodes()


  commentCluster : (event) =>

    cluster = d3.select(event.gesture.target).datum()

    text = cluster.get("comment") ? ""
    @showModal text, (text) =>

      cluster.set(comment: text)
      @graph.drawClusters()


  commentEdge : (event) =>

    edge = d3.select(event.gesture.target).datum()

    text = edge.get("comment") ? ""
    @showModal text, (text) =>

      edge.set(comment: text)
      @graph.drawEdges()


  showModal : (text, callback) ->

    $modal = $("#comment-modal")

    $textarea = $modal.find("textarea")
    $textarea.val(text)

    $modal.on "shown", -> $textarea.focus()

    $saveButton = $modal.find(".btn-primary")
    $saveButton.on "click", (event) ->

      $modal.modal("hide")

      text = $textarea.val()
      callback(text)

      $modal.off("shown")
      $saveButton.off("click")

    $modal.modal("show")




