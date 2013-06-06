### define
lib/event_mixin : EventMixin
d3 : d3
hammer : Hammer
jquery.mousewheel : Mousewheel
./process_view/graph : Graph
./process_view/gui : GUI
./process_view/selection_handler : SelectionHandler

../component/artifact_finder : ArtifactFinder
../component/artifact : Artifact
text!templates/process_view.html : ProcessViewTemplate

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

      @activate()
    )

    app.processView = @

  deactivate : ->

    @$el.find("#artifact-finder").off("dragstart")

    @$el.find(".toolbar a").off("click")

    @$el.find("image").off "dragstart"

    @gui.deactivate()
    @artifactFinder.deactivate()
    @zooming.deactive()
    @panning.deactive()
    @dragAndDrop.deactivate()
    @selectionHandler.deactivate()

  activate : ->

    @gui.activate()
    @artifactFinder.activate()
    @zooming.activate()
    @panning.activate()
    @dragAndDrop.activate()
    @selectionHandler.activate()

    # drag artifact into graph
    @$el.find("#artifact-finder").on( "dragstart", ".artifact-image", (e) -> e.preventDefault() )

    # change tool from toolbox
    processView = this
    @$el.find(".toolbar a").on "click", (event) -> processView.changeBehavior(this)

    @$el.find("image").on "dragstart", (e) ->
      e.preventDefault() #disable Firefox's native drag API

    Hammer($("#process-graph")[0]).on "mouseenter", ".node", (event) ->
      node = d3.select(event.target).datum()
      artifact = node.get("payload")
      wrappedArtifact = new Artifact(artifact, (->64), true, event.target)
      wrappedArtifact.onMouseEnter()


  changeBehavior : (selectedTool) =>

    graph = @graph

    toolBox = @$el.find(".toolbar a")
    behavior = switch selectedTool

      when toolBox[0] then new DrawClusterBehavior(graph, "standard")
      when toolBox[1] then new DrawClusterBehavior(graph, "standard") #twice is right
      when toolBox[2] then new DrawClusterBehavior(graph, "understand")
      when toolBox[3] then new DrawClusterBehavior(graph, "observe")
      when toolBox[4] then new DrawClusterBehavior(graph, "pov")
      when toolBox[5] then new DrawClusterBehavior(graph, "ideate")
      when toolBox[6] then new DrawClusterBehavior(graph, "prototype")
      when toolBox[7] then new DrawClusterBehavior(graph, "test")

    @selectionHandler.changeBehavior( behavior )
