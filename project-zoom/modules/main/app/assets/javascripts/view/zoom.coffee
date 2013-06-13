### define
jquery : $
underscore : _
app : app
lib/utils : Utils
lib/event_mixin : EventMixin
text!templates/zoom_slider.html : ZoomSliderTemplate
###

class Zoom

  min : 0
  max : 150
  step : 1

  constructor : ->

    EventMixin.extend(this)

    @$el = $(ZoomSliderTemplate)
    @el = @$el[0]

    @$input = @$el.find("input")

    @$input.attr({ @max, @min, @step })

    @level = +@$input.val()


  activate : ->

    @$el
      .on("change", "input", @onzoom)
      .on("click", ".plus", => @changeZoom(1) )
      .on("click", ".minus", => @changeZoom(-1) )


  deactivate : ->

    @$el
      .off("change", @zoom)
      .off("click", @changeZoomSlider)


  onzoom : (event) =>

    @level = +event.target.value
    @trigger("change", @level)
    return


  changeZoom : (delta, position) =>

    $input = @$el.find("input")
    @level = Utils.clamp(@min, (+$input.val() + delta) * @step, @max)
    $input.val(@level)
    @trigger("change", @level, position)
