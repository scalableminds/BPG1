### define
./behavior : Behavior
app : app
###

class DragAndDropBehavior extends Behavior

  constructor : (@$el, @graph) ->

    super(@graph)

  activate : ->

    @hammerContext = Hammer(@$el.find("#artifact-finder")[0])
      .on("dragend", "image", @addArtifact)
      .on("dragstart", "image", @dragStart)
      .on("drag", "image", @dragMove)


  deactivate : ->

    @hammerContext
      .off("dragend", @addArtifact)
      .off("dragstart", @dragStart)
      .off("drag", @dragMove)


  addArtifact : (event) =>

    imageElement = $(event.gesture.target)
    touch = event.gesture.touches[0]

    #is the mouse over the SVG?
    @offset = @graph.$svgEl.offset()

    if touch.pageX > @offset.left and touch.pageY > @offset.top

      position = @mouseToSVGLocalCoordinates(event)

      artifactId = imageElement.data("id")
      @graph.addNode(position.x, position.y, artifactId)

    @$preview.remove()


  dragStart : (event) =>

    return unless event.gesture

    $svgContainer = $(event.gesture.target).closest("svg").clone() #use clone for the preview, so that original stays within the artifacFinder

    @$preview = $("<div>", {class: "drag-preview"})
      .css(
        position : "absolute"
        width: "64px"
        height: "64px"
        "z-index": 100
      )

    @$preview.append($svgContainer)
    $(document.body).append(@$preview[0])

    @containerDimensions = $svgContainer[0].getBoundingClientRect() #correct dimension can only be retrieved after appending container to DOM


  dragMove : (event) =>

    return unless event.gesture

    x = event.gesture.touches[0].pageX - @containerDimensions.width / 2
    y = event.gesture.touches[0].pageY - @containerDimensions.height / 2

    #TODO use css transform instead of left/top, once they are prefix-free
    @$preview.css(
      left: x
      top: y
    )
