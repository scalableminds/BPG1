### define
lib/event_mixin : EventMixin
d3 : d3
hammer: Hammer
./process_view/interactive_graph : InteractiveGraph
./process_view/gui : GUI
./process_view/behavior/behavior : Behavior
./process_view/behavior/connect_behavior : ConnectBehavior
./process_view/behavior/drag_behavior : DragBehavior
./process_view/behavior/delete_behavior : DeleteBehavior
./process_view/behavior/draw_cluster_behavior : DrawClusterBehavior
./process_view/behavior/comment_behavior : CommentBehavior
./process_view/behavior/zoom_behavior : ZoomBehavior

../component/artifact_finder : ArtifactFinder
###

class ProcessView

  WIDTH = 960
  HEIGHT = 500
  time : null

  constructor : (@projectModel) ->

    @artifactFinder = new ArtifactFinder(@projectModel.get("artifacts"))
    @gui = new GUI(@artifactFinder)
    @projectModel.get "graphs/0", this, (graphModel) ->

      @graph = new InteractiveGraph(graphModel)

      @zooming = new ZoomBehavior(@graph)
      #@panning = new PanningBehavior()
      #@dragAndDrop = new DragAndDropBehavior()
      @activate()

    EventMixin.extend(this)


  deactivate : ->

    $("body").off("dragstart")
    @hammerContext
      .off("dragend")
      .off("touch")
      .off("release")

    $(".btn-group a").off("click")

    @graph.changeBehavior(new Behavior())
    @gui.deactivate()
    @artifactFinder.deactivate()
    @zooming.deactive()


  activate : ->

    @gui.activate()
    @artifactFinder.activate()
    @zooming.activate()

    # add new node
    #Hammer( $("svg")[0] ).on "tap", @addNode

    # drag artifact into graph
    $("body").on( "dragstart", "#artifact-finder .artifact-image", (e) -> e.preventDefault() )
    @hammerContext = Hammer(document.body).on "dragend", "#artifact-finder image", @addArtifact

    # change tool from toolbox
    processView = this
    $(".btn-group a").on "click", (event) -> processView.changeBehavior(this)

    Hammer($("#process-graph")[0]).on "tap", ".node", (event) ->
      node = d3.select(event.target).datum()
      artifact = node.get("payload")
      wrappedArtifact = new Artifact(artifact, (->64), true, event.target)
      wrappedArtifact.onMouseEnter()


  addArtifact : (evt) =>

    artifact = $(evt.gesture.target)
    touch = evt.gesture.touches[0]

    #is the mouse over the SVG?
    offset = $("#process-graph").offset()

    if touch.pageX > offset.left and touch.pageY > offset.top

      id = artifact.data("id")
      artifact = @artifactFinder.getArtifact(id)

      @on "view:zooming", artifact.resize

      @addNode(evt, artifact)
      artifact.resize() #call once so, that the right-sized image is loaded


  addNode : (evt, artifact = null) =>

    offset = $("#process-graph").offset()
    scaleValue = $(".zoom-slider input").val()

    x = event.gesture.touches[0].pageX - offset.left
    y = event.gesture.touches[0].pageY - offset.top

    x /= scaleValue
    y /= scaleValue

    @graph.addNode(x, y, artifact)


  changeBehavior : (selectedTool) =>

    graph = @graph
    graphContainer = @graph.graphContainer

    toolBox = $(".btn-group a")
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

    graph.changeBehavior( behavior )


