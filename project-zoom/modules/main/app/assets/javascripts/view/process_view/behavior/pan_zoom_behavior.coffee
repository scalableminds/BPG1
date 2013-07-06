### define
./behavior : Behavior
hammer : Hammer
app : app
view/wheel : Wheel
###

class PanZoomBehavior extends Behavior

  constructor : (@$el, @graph) ->

    @active = false
    super(@graph)

    app.on this,
      "behavior:enable_panning" : => @activate()
      "behavior:disable_panning": => @deactivate()

    @wheel = new Wheel(@$el.find(".graph")[0])

    @oldZoomLevel = 1
    @panZoom(.4)


  activate : ->

    unless @active

      @hammerContext = Hammer(@graph.svgEl)
        .on("drag", @pan)
        .on("dragstart", @panStart)

      @active = true

      app.view.process.on this, "zoom", @panZoom

      @wheel.activate()
      @wheel.on("delta", app.view.process.changeZoom)


  deactivate : ->

    if @active

      @hammerContext
        .off("drag", @pan)
        .off("dragstart", @panStart)

      @active = false
      app.view.process.off this, "zoom", @panZoom

      @wheel.deactivate()
      @wheel.off("delta", app.view.process.changeZoom)


  panStart : (event) =>

    @startPoint = @mouseToSVGLocalCoordinates(event)
    @startMatrix = @graph.graphContainer[0][0].getCTM()


  pan : (event) =>

    return unless event.gesture
    mouse = @mouseToSVGLocalCoordinates(event, @startMatrix.inverse())

    delta =
      x: mouse.x - @startPoint.x
      y: mouse.y - @startPoint.y

    transformationMatrix = @startMatrix.translate(delta.x, delta.y)
    @setCTM(transformationMatrix)

    app.trigger "behavior:panning"


  panZoom : (zoomLevel, position) =>

    $svg = $(@svgRoot)
    groupElement = @graph.graphContainer[0][0]

    if position

      mouse =
        x: position[0] - $svg.offset().left
        y: position[1] - $svg.offset().top

    else

      mouse =
        x: $svg.width() / 2
        y: $svg.height() / 2

    scale = zoomLevel / @oldZoomLevel

    p = @transformPointToLocalCoordinates(mouse)

    transformationMatrix = @svgRoot.createSVGMatrix()
      .translate(p.x, p.y)
      .scale(scale)
      .translate(-p.x, -p.y)

    @setCTM(groupElement.getCTM().multiply(transformationMatrix))

    @oldZoomLevel = zoomLevel

    app.trigger "behavior:zooming"

