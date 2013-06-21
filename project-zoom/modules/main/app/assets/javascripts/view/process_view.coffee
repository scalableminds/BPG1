### define
underscore : _
jquery : $
lib/event_mixin : EventMixin
d3 : d3
hammer : Hammer
jquery.mousewheel : Mousewheel

./process_view/graph : Graph
./process_view/gui : GUI
./process_view/artifact_finder : ArtifactFinder
./process_view/artifact : Artifact

text!templates/process_view.html : ProcessViewTemplate

./process_view/behavior/selection_handler : SelectionHandler
./process_view/behavior/draw_cluster_behavior : DrawClusterBehavior
./process_view/behavior/pan_zoom_behavior : PanZoomBehavior
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
      @panning = new PanZoomBehavior(@$el, @graph)
      @dragAndDrop = new DragAndDropBehavior(@$el, @graph, @graph.layouter)
      @selectionHandler = new SelectionHandler(@$el, @graph)

      @activate() if @pleaseActivate
    )

    @isActivated = false
    @pleaseActivate = false


  deactivate : ->

    @pleaseActivate = false

    return unless @isActivated

    @isActivated = false

    @$el.find("#artifact-finder").off("dragstart")

    @$el.find(".toolbar .dropdown-menu a").off("click", @changeBehavior)

    @$el.find("image").off("dragstart")

    @$el.off("transitionend", @windowResize)
    $(window).off("resize", @windowResize)

    @gui.deactivate()
    @artifactFinder.deactivate()
    @panning.deactivate()
    @dragAndDrop.deactivate()
    @selectionHandler.deactivate()

    @dispatcher.unregisterAll(this)


  activate : ->

    return if @isActivated

    if @graph

      @isActivated = true
      @pleaseActivate = false

      @$el.on("transitionend", @windowResize)
      $(window).on("resize", @windowResize)

      @gui.activate()
      @artifactFinder.activate()
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

    else

      @pleaseActivate = true


  windowResize : =>

    @artifactFinder.windowResize()
    @gui.windowResize()
    return


  changeBehavior : ({ target : selectedTool}) =>

    { graph, $el:element } = @

    toolBox = @$el.find(".toolbar .dropdown-menu a")
    behavior = switch selectedTool

      when toolBox[0] then new DrawClusterBehavior(graph, element, "freeform")
      when toolBox[1] then new DrawClusterBehavior(graph, element, "Understand")
      when toolBox[2] then new DrawClusterBehavior(graph, element, "Observe")
      when toolBox[3] then new DrawClusterBehavior(graph, element, "POV")
      when toolBox[4] then new DrawClusterBehavior(graph, element, "Ideate")
      when toolBox[5] then new DrawClusterBehavior(graph, element, "Prototype")
      when toolBox[6] then new DrawClusterBehavior(graph, element, "Test")

    @selectionHandler.changeBehavior( behavior )
