### define
./behavior : Behavior
jquery.mousewheel : Mousewheel

###

class ZoomBehavior extends Behavior

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

    scaleValue = $(".zoom-slider input").val()

    @graph.graphContainer.attr("transform", "scale( #{scaleValue} )") #"translate(" + d3.event.translate + ")
    @graph.drawNodes() #refresh node


  changeZoomSlider : (delta) ->

    $slider = $(".zoom-slider input")
    sliderValue = parseFloat($slider.val())
    $slider.val( sliderValue + delta )

    @zoom()