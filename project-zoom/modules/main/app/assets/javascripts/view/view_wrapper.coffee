### define
jquery : $
underscore : _
app : app
lib/event_mixin : EventMixin
###

PreventableFunction = (func) ->

  wrappedFunc = ->
    func.apply(this, arguments)

  wrappedFunc.stop = ->
    func = ->

  wrappedFunc


class ViewWrapper

  constructor : (@viewClass, @argsMaker, @zoomConverter = _.identity) ->

    EventMixin.extend(this)
    @zoom = 0
    @isActivated = false
    @$placeholder = $("<div>", class : "placeholder-view")
    @nextAction = null


  view : null

  make : (args...) ->

    unless @view
      @showPlaceholder()

      viewArgs = @argsMaker(args...)

      model = viewArgs[0]
      model.loaded.done(
        => @hidePlaceholder()
      )
      @view = new @viewClass(viewArgs...)
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
      "transformOrigin" : if position then "#{position[0]}px #{position[1] - 41}px" else ""
    )
    @$placeholder.css(
      "transform" : "scale(#{scale})"
      "transformOrigin" : if position then "#{position[0]}px #{position[1] - 41}px" else ""
    )


  resetZoom : ->

    @make()
    @view.$el.css(
      "transform" : ""
      "transformOrigin" : ""
    )
    @$placeholder.css(
      "transform" : ""
      "transformOrigin" : ""
    )


  hidePlaceholder : ->
    @$placeholder.remove()

  showPlaceholder : ->
    @$placeholder.appendTo(".content")

  changeZoom : (delta, position) =>

    app.view.zoom.changeZoom(delta, position)


  onGlobalZoom : (level, position) =>

    @zoom = Math.max(@zoomConverter(level), 0)
    @trigger("zoom", @zoom, position)
    



