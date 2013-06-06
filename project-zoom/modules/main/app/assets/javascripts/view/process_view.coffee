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

./process_view/behavior/behavior : Behavior
./process_view/behavior/connect_behavior : ConnectBehavior
./process_view/behavior/drag_behavior : DragBehavior
./process_view/behavior/delete_behavior : DeleteBehavior
./process_view/behavior/draw_cluster_behavior : DrawClusterBehavior
./process_view/behavior/comment_behavior : CommentBehavior
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

      @activate()
    )


  deactivate : ->

    @$el.find("#artifact-finder").off("dragstart")

    @$el.find(".toolbar a")
      .off("click")

    @graph.changeBehavior(new Behavior())
    @gui.deactivate()
    @artifactFinder.deactivate()
    @zooming.deactivate()
    @panning.deactivate()
    @dragAndDrop.deactivate()


  activate : ->

    @gui.activate()
    @artifactFinder.activate()
    @zooming.activate()
    @panning.activate()
    @dragAndDrop.activate()

    # drag artifact into graph
    @$el.find("#artifact-finder").on( "dragstart", ".artifact-image", (e) -> e.preventDefault() )

    # change tool from toolbox
    processView = this
    @$el.find(".toolbar a").on "click", (event) -> processView.changeBehavior(this)

    Hammer($("#process-graph")[0]).on "mouseenter", ".node", (event) ->
      node = d3.select(event.target).datum()
      artifact = node.get("payload")
      wrappedArtifact = new Artifact(artifact, (->64), true, event.target)
      wrappedArtifact.onMouseEnter()


  changeBehavior : (selectedTool) =>

    graph = @graph
    graphContainer = @graph.graphContainer

    toolBox = @$el.find(".toolbar a")
    behavior = switch selectedTool

      when toolBox[0] then new DragBehavior(graph)
      when toolBox[1] then new ConnectBehavior(graph, graphContainer)
      when toolBox[2] then new DeleteBehavior(graph)
      when toolBox[3] then new DrawClusterBehavior(graph, graphContainer, "standard")
      when toolBox[4] then new DrawClusterBehavior(graph, graphContainer, "standard") #twice is right
      when toolBox[5] then new DrawClusterBehavior(graph, graphContainer, "understand")
      when toolBox[6] then new DrawClusterBehavior(graph, graphContainer, "observe")
      when toolBox[7] then new DrawClusterBehavior(graph, graphContainer, "pov")
      when toolBox[8] then new DrawClusterBehavior(graph, graphContainer, "ideate")
      when toolBox[9] then new DrawClusterBehavior(graph, graphContainer, "prototype")
      when toolBox[10] then new DrawClusterBehavior(graph, graphContainer, "test")
      when toolBox[11] then new CommentBehavior(graph)

    if behavior instanceof ConnectBehavior or behavior instanceof DrawClusterBehavior
      @panning.deactivate()
    else
      @panning.activate()

    graph.changeBehavior( behavior )

