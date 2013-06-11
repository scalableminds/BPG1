### define
jquery : $
underscore : _
app : app
lib/event_mixin : EventMixin
###

class ViewWrapper

  constructor : (@maker, @zoomConverter) ->

    EventMixin.extend(this)
    @zoom = 0
    @isActivated = false


  view : null

  make : (args...) ->

    unless @view
      @view = @maker(args...)
      $(".content").append(@view.el)
      @isActivated = false


  kill : ->

    if @view
      @deactivate()
      @view.$el.remove()
      @view = null
      @isActivated = false


  activate : ->

    @make()
    unless @isActivated
      @view.activate()
      app.view.zoom.on(this, "change", @onGlobalZoom)
      @onGlobalZoom(app.view.zoom.level)
      @isActivated = true

    @view.$el.removeClass("inactive")


  deactivate : ->

    @make()
    if @isActivated
      @view.deactivate()
      app.view.zoom.off(this, "change", @onGlobalZoom)
      @isActivated = false

    @view.$el.addClass("inactive")


  setZoom : (scale, position) ->

    @make()
    @view.$el.css(
      "transform" : "scale(#{scale})"
      "transformOrigin" : if position then "#{position[0]}px #{position[1]}px" else ""
    )

  resetZoom : ->

    @make()
    @view.$el.css(
      "transform" : ""
      "transformOrigin" : ""
    )


  changeZoom : (delta, position) =>

    app.view.zoom.changeZoom(delta, position)


  onGlobalZoom : (level, position) =>

    @zoom = Math.max(@zoomConverter(level), 0)
    @trigger("zoom", @level, position)
    



