### define
lib/event_mixin : EventMixin
d3 : d3
hammer : Hammer
jquery.mousewheel : Mousewheel
./process_view/graph : Graph
./process_view/gui : GUI
./process_view/behavior/behavior : Behavior
./process_view/behavior/connect_behavior : ConnectBehavior
./process_view/behavior/drag_behavior : DragBehavior
./process_view/behavior/delete_behavior : DeleteBehavior
./process_view/behavior/draw_cluster_behavior : DrawClusterBehavior
./process_view/behavior/comment_behavior : CommentBehavior
../component/artifact_finder : ArtifactFinder
text!templates/process_view.html : ProcessViewTemplate
###

class ProcessView

  WIDTH = 960
  HEIGHT = 500
  time : null

  constructor : (@projectModel) ->

    EventMixin.extend(this)

    @$el = $(ProcessViewTemplate)
    @el = @$el[0]
    $(".content").append(@$el)

    @artifactFinder = new ArtifactFinder(@projectModel.get("artifacts"))
    @gui = new GUI(@$el, @artifactFinder)
    @projectModel.get("graphs/0", this, (graphModel) =>
      @graph = new Graph(@$el.find(".graph")[0], graphModel)
    )

    @activate()


  deactivate : ->

    @$el.find("#artifact-finder").off("dragstart")
    Hammer(@$el.find("#artifact-finder")[0])
      .off("dragend")
    Hammer(document.body)
      .off("touch")
      .off("release")

    @$el.find(".toolbar a")
      .off("click")
    @$el.find(".zoom-slider")
      .off("change")
      .off("click")

    @graph.changeBehavior(new Behavior())
    @gui.deactivate()
    @artifactFinder.deactivate()


  activate : ->

    @gui.activate()
    @artifactFinder.activate()

    # drag artifact into graph
    @$el.find("#artifact-finder").on( "dragstart", ".artifact-image", (e) -> e.preventDefault() )
    Hammer(@$el.find("#artifact-finder")[0]).on("dragend", "image", @addArtifact)

    # change tool from toolbox
    processView = this
    @$el.find(".toolbar a").on "click", (event) -> processView.changeBehavior(this)

    Hammer($("#process-graph")[0]).on "tap", ".node", (event) ->
      node = d3.select(event.target).datum()
      artifact = node.get("payload")
      wrappedArtifact = new Artifact(artifact, (->64), true, event.target)
      wrappedArtifact.onMouseEnter()

    # zooming
    @$el.find(".zoom-slider")
      .on("change", "input", @zoom)
      .on("click", ".plus", => @changeZoomSlider(0.1) )
      .on("click", ".minus", => @changeZoomSlider(-0.1) )

    do =>

      mouseDown = false

      Hammer(document.body)
        .on("touch", -> mouseDown = true; return )
        .on("release", -> mouseDown = false; return )

      @$el.find(".graph").on "mousewheel", (evt, delta, deltaX, deltaY) =>

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
    offset = @$el.find("#process-graph").offset()

    if touch.pageX > offset.left and touch.pageY > offset.top

      id = artifact.data("id")
      artifact = @artifactFinder.getArtifact(id)

      @on "view:zooming", artifact.resize

      @addNode(evt, artifact)
      artifact.resize() #call once so, that the right-sized image is loaded


  addNode : (evt, artifact = null) =>

    offset = @$el.find("#process-graph").offset()
    scaleValue = @$el.find(".zoom-slider input").val()

    x = event.gesture.touches[0].pageX - offset.left
    y = event.gesture.touches[0].pageY - offset.top

    x /= scaleValue
    y /= scaleValue

    @graph.addNode(x, y, artifact)


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

    graph.changeBehavior( behavior )


  zoom : (event) =>

    scaleValue = @$el.find(".zoom-slider input").val()

    @graph.graphContainer.attr("transform", "scale( #{scaleValue} )") #"translate(" + d3.event.translate + ")
    @graph.drawNodes() #refresh node

  changeZoomSlider : (delta) ->

    $slider = @$el.find(".zoom-slider input")
    sliderValue = parseFloat($slider.val())
    $slider.val( sliderValue + delta )

    @zoom()