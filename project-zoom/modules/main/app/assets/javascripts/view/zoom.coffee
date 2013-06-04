### define
jquery : $
underscore : _
app : app
lib/event_mixin : EventMixin
text!templates/zoom_slider.html : ZoomSliderTemplate
###

class Zoom

  constructor : ->

    EventMixin.extend(this)

    @el = $(ZoomSliderTemplate)
    @$el = $(@el)

    @level = @el.val()


  activate : ->

    @$el
      .on("change", "input", @onzoom)
      .on("click", ".plus", => @changeZoom(.1) )
      .on("click", ".minus", => @changeZoom(-.1) )


  deactivate : ->

    @$el
      .off("change", @zoom)
      .off("click", @changeZoomSlider)


  onzoom : (event) =>

    @level = +event.target.value
    @trigger("change", @level)
    return


  changeZoom : (delta) ->

    $input = @$el.find("input")
    @level = +$input.val() + delta
    $input.val(@level)
    @trigger("change", @level)
