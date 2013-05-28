### define
lib/event_mixin : EventMixin
d3 : d3
hammer: Hammer
jquery.mousewheel : Mousewheel
./process_view/interactive_graph : InteractiveGraph
./process_view/gui : GUI
./process_view/behavior/behavior : Behavior
./process_view/behavior/connect_behavior : ConnectBehavior
./process_view/behavior/drag_behavior : DragBehavior
./process_view/behavior/delete_behavior : DeleteBehavior
./process_view/behavior/draw_cluster_behavior : DrawClusterBehavior
./process_view/behavior/comment_behavior : CommentBehavior
../component/artifact_finder : ArtifactFinder
###

class ProcessView

  WIDTH = 960
  HEIGHT = 500
  time : null

  constructor : (@projectModel) ->

    @artifactFinder = new ArtifactFinder()
    @gui = new GUI(@artifactFinder)
    @projectModel.get "graphs/0", this, (graphModel) ->

      @graph = new InteractiveGraph(graphModel)


    EventMixin.extend(this)

    @activate()


  deactivate : ->

    $("body").off("dragstart")
    @hammerContext
      .off("dragend")
      .off("touch")
      .off("release")

    $(".btn-group a").off("click")
    $(".zoom-slider")
      .off("change")
      .off("click")

    @graph.changeBehavior(new Behavior())
    @gui.deactivate()
    @artifactFinder.deactivate()

    #what about the html?
    #$("svg").hide()
    #$("#artifact-finder").hide()


  activate : ->

    @gui.activate()
    @artifactFinder.activate()

    # add new node
    #Hammer( $("svg")[0] ).on "tap", @addNode

    # drag artifact into graph
    $("body").on( "dragstart", "#artifact-finder .artifact-image", (e) -> e.preventDefault() )
    @hammerContext = Hammer(document.body).on "dragend", "#artifact-finder image", @addArtifact

    # change tool from toolbox
    processView = this
    $(".btn-group a").on "click", (event) -> processView.changeBehavior(this)

    # zooming
    $(".zoom-slider")
      .on("change", "input", @zoom)
      .on("click", ".plus", => @changeZoomSlider(0.1) )
      .on("click", ".minus", => @changeZoomSlider(-0.1) )

    do =>

      mouseDown = false

      @hammerContext
        .on("touch", -> mouseDown = true; return )
        .on("release", -> mouseDown = false; return )

      $(".graph").on "mousewheel", (evt, delta, deltaX, deltaY) =>

        evt.preventDefault()
        return if mouseDown
        if deltaY > 0
          @changeZoomSlider(0.1)
        else
          @changeZoomSlider(-0.1)


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


  addNode : (evt, nodeId, artifact = null) =>

    offset = $("#process-graph").offset()
    scaleValue = $(".zoom-slider input").val()

    x = event.gesture.touches[0].pageX - offset.left
    y = event.gesture.touches[0].pageY - offset.top

    x /= scaleValue
    y /= scaleValue

    @graph.addNode(x, y, nodeId, artifact)


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


  zoom : (event) =>

    scaleValue = $(".zoom-slider input").val()

    @graph.graphContainer.attr("transform", "scale( #{scaleValue} )") #"translate(" + d3.event.translate + ")
    @trigger("view:zooming")


  changeZoomSlider : (delta) ->

    $slider = $(".zoom-slider input")
    sliderValue = parseFloat($slider.val())
    $slider.val( sliderValue + delta )

    @zoom()