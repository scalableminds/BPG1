### define
lib/event_mixin : EventMixin
d3 : d3
hammer : Hammer
jquery.mousewheel : Mousewheel
./process_view/graph : Graph
./process_view/gui : GUI

../component/artifact_finder : ArtifactFinder
../component/artifact : Artifact
text!templates/process_view.html : ProcessViewTemplate

./process_view/behavior/selection_handler : SelectionHandler
./process_view/behavior/draw_cluster_behavior : DrawClusterBehavior
./process_view/behavior/zoom_behavior : ZoomBehavior
./process_view/behavior/pan_behavior : PanBehavior
./process_view/behavior/drag_and_drop_behavior : DragAndDropBehavior

###

class ProcessView

  WIDTH = 960
  HEIGHT = 500
  time : null

  constructor : (@projectModel) ->

    EventMixin.extend(this)

    @$el = $(ProcessViewTemplate)
    @el = @$el[0]

    @artifactFinder = new ArtifactFinder(@projectModel.get("artifacts"))
    @gui = new GUI(@$el, @artifactFinder)
    @projectModel.get("graphs/0", this, (graphModel) =>

      @graph = new Graph(@$el.find(".graph")[0], graphModel, @artifactFinder)
      @zooming = new ZoomBehavior(@$el, @graph)
      @panning = new PanBehavior(@$el, @graph)
      @dragAndDrop = new DragAndDropBehavior(@$el, @graph)
      @selectionHandler = new SelectionHandler(@$el, @graph)
    )

    @isActivated = false


  deactivate : ->

    return unless @isActivated

    @$el.find("#artifact-finder").off("dragstart")

    @$el.find(".toolbar .dropdown-menu a").off("click", @changeBehavior)

    @$el.find("image").off("dragstart")

    @$el.off("transitionend", @windowResize)
    $(window).off("resize", @windowResize)

    @gui.deactivate()
    @artifactFinder.deactivate()
    @zooming.deactivate()
    @panning.deactivate()
    @dragAndDrop.deactivate()
    @selectionHandler.deactivate()


  activate : ->

    return if @isActivated

    @projectModel.get("graphs/0", this, =>

      @$el.on("transitionend", @windowResize)
      $(window).on("resize", @windowResize)

      @gui.activate()
      @artifactFinder.activate()
      @zooming.activate()
      @panning.activate()
      @dragAndDrop.activate()
      @selectionHandler.activate()

      # drag artifact into graph
      @$el.find("#artifact-finder").on( "dragstart", ".artifact-image", (e) -> e.preventDefault() )

      # change tool from toolbox
      @$el.find(".toolbar .dropdown-menu a").on("click", @changeBehavior)

      @$el.find("image").on "dragstart", (e) ->
        e.preventDefault() #disable Firefox's native drag API

      Hammer($("#process-graph")[0]).on "mouseenter", ".node", (event) ->
        node = d3.select(event.target).datum()
        artifact = node.get("payload")
        wrappedArtifact = new Artifact(artifact, (->64), true, event.target)
        wrappedArtifact.onMouseEnter()

      @isActivated = true

    )


  windowResize : =>

    @artifactFinder.windowResize()
    @gui.windowResize()
    return


  changeBehavior : ({ target : selectedTool}) =>

    { graph, $el:element } = @

    toolBox = @$el.find(".toolbar .dropdown-menu a")
    behavior = switch selectedTool

      when toolBox[0] then new DrawClusterBehavior(graph, element, "standard")
      when toolBox[1] then new DrawClusterBehavior(graph, element, "understand")
      when toolBox[2] then new DrawClusterBehavior(graph, element, "observe")
      when toolBox[3] then new DrawClusterBehavior(graph, element, "pov")
      when toolBox[4] then new DrawClusterBehavior(graph, element, "ideate")
      when toolBox[5] then new DrawClusterBehavior(graph, element, "prototype")
      when toolBox[6] then new DrawClusterBehavior(graph, element, "test")

    @selectionHandler.changeBehavior( behavior )
