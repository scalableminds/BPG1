### define
./behavior : Behavior
jquery.mousewheel : Mousewheel
app : app
###

class ZoomBehavior extends Behavior

  constructor : (@$el, @graph) ->

    @zoom(.5)


  activate : ->

    app.view.zoom.on(this, "change", @zoom)

    do =>

      mouseDown = false

      @mouseDownHandler = -> mouseDown = true; return
      @mouseUpHandler = -> mouseDown = false; return
      @mouseWheelHandler = (evt, delta, deltaX, deltaY) ->

        evt.preventDefault()
        return if mouseDown
        if deltaY != 0
          app.view.zoom.changeZoom(deltaY / Math.abs(deltaY) * .1, [ evt.pageX, evt.pageY ])


      @hammerContext = Hammer(document.body)
        .on("touch", @mouseDownHandler)
        .on("release", @mouseUpHandler)

      @$el.find(".graph").on("mousewheel", @mouseWheelHandler)

    @zoom()



  deactivate : ->

    app.view.zoom.off(this, "change", @zoom)

    @$el.find(".graph").off("mousewheel", @mouseWheelHandler)

    @hammerContext
      .off("touch", @mouseDownHandler)
      .off("release", @mouseUpHandler)


  zoom : (scaleValue = app.view.zoom.level) =>

    graphContainer = @graph.graphContainer

    transformation = d3.transform(graphContainer.attr("transform"))
    transformation.scale = [scaleValue, scaleValue]

    graphContainer.attr("transform", transformation.toString())

    app.trigger "behavior:zooming"
