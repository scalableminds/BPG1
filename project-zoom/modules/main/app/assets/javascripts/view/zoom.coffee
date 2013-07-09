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
  startValue : 2

  constructor : ->

    EventMixin.extend(this)

    @$el = $(ZoomSliderTemplate)
    @el = @$el[0]

    @$input = @$el.find("input")

    @$input.attr({ @max, @min, @step, value : @startValue })

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

    @setZoom(+event.target.value)


  setZoom : (value, position) ->

    @level = Utils.clamp(@min, value, @max)
    $input = @$el.find("input")
    $input.val(@level)
    @trigger("change", @level, position)
    return


  changeZoom : (delta, position) =>

    @setZoom((@level + delta) * @step, position)
    

