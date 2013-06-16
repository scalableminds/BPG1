### define
./behavior : Behavior
hammer : Hammer
app : app
###

class PanBehavior extends Behavior

  constructor : (@$el, @graph) ->

    @active = false
    super(@graph)

    app.on this,
      "behavior:enable_panning" : => @activate()
      "behavior:disable_panning": => @deactivate()


  activate : ->

    unless @active

      @oldZoomLevel = app.view.process.zoom

      @hammerContext = Hammer(@graph.svgEl)
        .on("drag", @pan)
        .on("dragstart", @panStart)

      @active = true
      app.view.process.on this, "zoom", @panAfterZooming


  deactivate : ->

    if @active

      @hammerContext
        .off("drag", @pan)
        .off("dragstart", @panStart)

      @active = false
      app.view.zoom.off this, "change"


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


  panAfterZooming : (zoomLevel, position) =>

    $svg = @$el.find("#process-graph")
    svgRoot = $svg[0]
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


    transformationMatrix = svgRoot.createSVGMatrix()
      .translate(p.x, p.y)
      .scale(scale)
      .translate(-p.x, -p.y)

    @setCTM(groupElement.getCTM().multiply(transformationMatrix))

    @oldZoomLevel = zoomLevel

