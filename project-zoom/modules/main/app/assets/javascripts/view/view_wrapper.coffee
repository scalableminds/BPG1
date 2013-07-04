### define
jquery : $
underscore : _
app : app
lib/event_mixin : EventMixin
###

ActionQueue = ->

  callbacks = []
  isResolved = false

  this.done = (func) ->
    if isResolved
      func()
    else
      callbacks.push(func)
    this

  this.stop = ->
    callbacks = []
    return

  this.resolve = ->
    return this if isResolved
    isResolved = true
    callback() while callback = callbacks.shift()
    this

  this.callbacks = -> callbacks.slice(0)

  return



class ViewWrapper

  constructor : (@viewClass, @argsMaker, @zoomConverter = _.identity) ->

    EventMixin.extend(this)
    @zoom = 0
    @isActivated = false
    @$placeholder = $("<div>", class : "placeholder-view").append("""<i class="icon-refresh"></i>""")
    @placeholderIsActive = false
    @actionQueue = null


  view : null

  make : (args...) ->

    unless @actionQueue
      @actionQueue = new ActionQueue()
      viewArgs = @argsMaker(args...)

      model = viewArgs[0]
      model.loaded.done => 
        @actionQueue?.resolve()
        return

      viewArgs = @argsMaker(args...)
      unless @view
        @showPlaceholder()
        @actionQueue.done =>
          @hidePlaceholder()
          @view = new @viewClass(viewArgs...)
          $(".content").append(@view.el)
          @isActivated = false

    unless @view
      @showPlaceholder()
      @actionQueue.done =>
        unless @view
          @hidePlaceholder()
          @view = new @viewClass(@argsMaker(args...)...)
          $(".content").append(@view.el)
          @isActivated = false

    @actionQueue


      
  kill : ->

    @hidePlaceholder()
    if @actionQueue
      @actionQueue.stop()
      @actionQueue = null

    if @view
      @deactivate()
      @view.$el.remove()
      @view = null
      @isActivated = false


  activate : ->

    @make().done =>
      unless @isActivated
        @view.activate()
        app.view.zoom.on(this, "change", @onGlobalZoom)
        @onGlobalZoom(app.view.zoom.level)
        @isActivated = true

      @view?.$el.removeClass("inactive")
    
    

  deactivate : ->

    @make().done =>
      if @isActivated
        @view.deactivate()
        app.view.zoom.off(this, "change", @onGlobalZoom)
        @isActivated = false

      @view.$el.addClass("inactive")
    

  setZoom : (scale, position) ->

    @make().done =>
      @view.$el.css(
        "transform" : "scale(#{scale})"
        "transformOrigin" : if position then "#{position[0]}px #{position[1] - 41}px" else ""
      )
    @$placeholder.css(
      "transform" : "scale(#{scale})"
      "transformOrigin" : if position then "#{position[0]}px #{position[1] - 41}px" else ""
    )


  resetZoom : ->

    @make().done =>
      @view.$el.css(
        "transform" : ""
        "transformOrigin" : ""
      )
    
    @$placeholder.css(
      "transform" : ""
      "transformOrigin" : ""
    )


  hidePlaceholder : ->
    if @placeholderIsActive
      @$placeholder.remove()
      @placeholderIsActive = false


  showPlaceholder : ->
    unless @placeholderIsActive
      @$placeholder.appendTo(".content")
      @placeholderIsActive = true


  changeZoom : (delta, position) =>

    app.view.zoom.changeZoom(delta, position)


  onGlobalZoom : (level, position) =>

    @zoom = Math.max(@zoomConverter(level), 0)
    @trigger("zoom", @zoom, position)
    



