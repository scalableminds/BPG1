### define
./behavior : Behavior
./connect_behavior : ConnectBehavior
./drag_behavior : DragBehavior
./delete_behavior : DeleteBehavior
./comment_behavior : CommentBehavior
app : app
d3 : d3
###

class SelectionHandler extends Behavior

  constructor : (@$el, @graph) ->

    super(@graph)

    @behaviors =
      DRAG : new DragBehavior(@graph)
      CONNECT : new ConnectBehavior(@graph)
      DELETE : new DeleteBehavior(@graph)
      COMMENT : new CommentBehavior(@graph)
      IDLE : new Behavior(@graph)

    @selection = null

    @currentBehavior = @behaviors.IDLE


  activate : ->

    @createToolBar()

    @hammerContext = Hammer( @graph.svgEl )
      .on("tap", "svg", @unselect)
      .on("tap", ".node", @selectNode)
      .on("tap", ".cluster", @selectCluster)
      .on("dragstart", ".node", @selectNode)
      .on("dragstart", ".cluster", @selectCluster)

    @$tools.find(".btn").on("tap", @selectBehavior)

    app.on this,
      "behavior:done" : =>
        if @selection
          @changeBehavior( @behaviors.DRAG )
        else
          @changeBehavior( @behaviors.IDLE )
      "behavior:delete" : => @unselect()
      "behavior:drag" : => @positionToolbar()
      "behavior:zooming" : => @positionToolbar()
      "behavior:panning" : => @positionToolbar()


  deactivate : ->

    @$tools.remove()
    @$tools.find(".btn").off("tap", @changeBehavior)
    @$tools = null

    @hammerContext
      .off("tap", @unselect)
      .off("tap", @selectNode)
      .off("tap", @selectCluster)
      .off("dragstart", @selectNode)
      .off("dragstart", @selectCluster)

    @dispatcher.unregisterAll(this)


  selectNode : (event) =>

    event.stopPropagation()

    return unless event.gesture
    return if @currentBehavior instanceof ConnectBehavior #unclean workaround

    @selection = event.gesture.target
    @selection.position = @mousePosition(event)

    node = d3.select(@selection).datum()
    artifact = node.get("payload/resources").find((a) -> a.get("typ") == "default")
    downloadURL = "/artifacts/#{node.get("payload/id")}/default/#{artifact.get("name")}"

    @positionToolbar()
    @$tools.find("#download").attr("href", downloadURL)
    @$tools
      .removeClass("node cluster")
      .addClass("node") # make sure we only add the class once
      .show()

    @changeBehavior( @behaviors.DRAG )


  selectCluster : (event) =>

    return unless event.gesture

    @selection = event.gesture.target
    @selection.position = @mousePosition(event)

    @positionToolbar()
    @$tools
      .removeClass("cluster node")
      .addClass("cluster") # make sure we only add the class once
      .show()

    @changeBehavior( @behaviors.DRAG )


  createToolBar : ->

    unless @$tools
      template = """
      <div id="tool-bar">
        <div class="float-container">
          <a class="btn" href="#" id="comment"><i class="icon-comment"></i></a>
          <a class="btn" href="#" id="connect"><i class="icon-arrow-right"></i></a>
          <a class="btn" href="#" id="delete"><i class="icon-trash"></i></a>
          <a class="btn" href="#" id="download" target="_blank"><i class="icon-download-alt"></i></a>
        </div>
      </div>
      """

      @$tools = $(template)
      @$tools.hide()

      @$el.append(@$tools)

  positionToolbar : ->

    if @selection

      boundingBox = @selection.getBoundingClientRect()
      buttonWidth = 48

      @$tools.css(
        left: boundingBox.left
        top: boundingBox.top - buttonWidth # offset due to parents relative position
        width: boundingBox.width + buttonWidth
        height: boundingBox.height
      )

    else
      @$tools.hide()

  selectBehavior : (event) =>

    if $(event.target).is(".btn")
      $target = $(event.target)
    else
      $target = $(event.target).closest(".btn")

    behavior = switch $target.attr("id")
      when "delete" then @behaviors.DELETE
      when "comment" then @behaviors.COMMENT
      when "connect" then @behaviors.CONNECT
      else @behaviors.DRAG

    @changeBehavior( behavior )


  unselect : =>

    @selection = null
    @$tools.hide()
    @changeBehavior( @behaviors.IDLE )


  changeBehavior : (behavior) ->

    @currentBehavior.deactivate()
    @currentBehavior = behavior
    @currentBehavior.activate(@selection)
