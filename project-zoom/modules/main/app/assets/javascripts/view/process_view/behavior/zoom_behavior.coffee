### define
./behavior : Behavior
jquery.mousewheel : Mousewheel
###

class ZoomBehavior extends Behavior

  constructor : (@graph) ->


  activate : ->

    $(".zoom-slider")
      .on("change", "input", @zoom)
      .on("click", ".plus", => @changeZoomSlider(0.1) )
      .on("click", ".minus", => @changeZoomSlider(-0.1) )

    do =>

      mouseDown = false

      @hammerContext = Hammer(document.body)
        .on("touch", -> mouseDown = true; return )
        .on("release", -> mouseDown = false; return )

      $(".graph").on "mousewheel", (evt, delta, deltaX, deltaY) =>

        evt.preventDefault()
        return if mouseDown
        if deltaY > 0
          @changeZoomSlider(0.1)
        else
          @changeZoomSlider(-0.1)




  deactivate : ->

    $(".zoom-slider")
      .off("change")
      .off("click")

    @hammerContext
      .off("dragend")
      .off("touch")


  zoom : (event) =>

    graphContainer = @graph.graphContainer

    scaleValue = $(".zoom-slider input").val()

    transformation = d3.transform(graphContainer.attr("transform"))
    transformation.scale = [scaleValue, scaleValue]

    graphContainer.attr("transform", transformation.toString())

    app.trigger "zooming"

  changeZoomSlider : (delta) ->

    $slider = $(".zoom-slider input")
    sliderValue = parseFloat($slider.val())
    $slider.val( sliderValue + delta )

    @zoom()