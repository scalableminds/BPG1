### define
./behavior : Behavior
app: app
d3 : d3
text!templates/process_view_modal.html : ModalTemplate
###

class CommentBehavior extends Behavior

  activate : (@element) ->

    element = d3.select(@element)

    if element.classed("node")
      @commentNode()

    if element.classed("cluster")
      @commentCluster()


  commentNode : (event) =>

    node = d3.select(@element).datum()

    text = node.get("comment") ? ""
    @showModal text, (text) =>

      node.set(comment: text)
      @graph.drawNodes()
      app.trigger "behavior:done"


  commentCluster : (event) =>

    cluster = d3.select(@element).datum()

    text = cluster.get("comment") ? ""
    @showModal text, (text) =>

      cluster.set(comment: text)
      @graph.drawClusters()
      app.trigger "behavior:done"


  commentEdge : (event) =>

    edge = d3.select(@element).datum()

    text = edge.get("comment") ? ""
    @showModal text, (text) =>

      edge.set(comment: text)
      @graph.drawEdges()
      app.trigger "behavior:done"


  showModal : (text, callback) ->

    $modal = $(ModalTemplate)

    $textarea = $modal.find("textarea")
    $textarea.val(text)

    $modal.one "shown", -> $textarea.focus()

    $saveButton = $modal.find(".btn-primary")
    $saveButton.on "click", (event) ->

      $modal.modal("hide")
      $modal.remove()

      text = $textarea.val()
      callback(text)

      $saveButton.off("click")

    $modal.modal("show")




