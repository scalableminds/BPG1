### define
./behavior : Behavior
app : app
###

class DragAndDropBehavior extends Behavior

  constructor : (@$el, @graph) ->


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

      mouse = @mousePosition(event)
      artifactId = imageElement.data("id")

      translation = d3.transform(@graph.graphContainer.attr("transform")).translate

      position =
        x: mouse.x - translation[0] / app.view.process.zoom
        y: mouse.y - translation[1] / app.view.process.zoom

      @graph.addNode(position.x, position.y, artifactId)

    @$preview.remove()


  dragStart : (event) =>

    return unless event.gesture

    svgContainer = $(event.gesture.target).closest("svg").clone() #use clone for the preview, so that original stays within the artifacFinder
    mouse = @mousePosition(event, false)

    @$preview = $("<div>", {class: "drag-preview"})
      .css(
        position : "absolute"
        left: mouse.x #better use css transform, once it is prefix-free
        top: mouse.y
        width: "64px"
        height: "64px"
        opacity: 0.8
        "z-index": 100
      )

    @$preview.append(svgContainer)
    @$el.append(@$preview)


  dragMove : (event) =>

    return unless event.gesture

    mouse = @mousePosition(event, false)

    @$preview.css(
      left: mouse.x
      top: mouse.y
    )
