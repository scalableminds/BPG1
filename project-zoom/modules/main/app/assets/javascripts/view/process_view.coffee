### define
lib/event_mixin : EventMixin
d3 : d3
hammer: Hammer
jquery.mousewheel : Mousewheel
./process_view/interactive_graph : InteractiveGraph
./process_view/gui : GUI
./process_view/behavior/connect_behavior : ConnectBehavior
./process_view/behavior/drag_behavior : DragBehavior
./process_view/behavior/delete_behavior : DeleteBehavior
./process_view/behavior/draw_cluster_behavior : DrawClusterBehavior
../component/artifact_finder : ArtifactFinder
../component/artifact : Artifact
###

class ProcessView

  WIDTH = 960
  HEIGHT = 500
  time : null



  constructor : (@projectModel) ->

    EventMixin.extend(this)
    @initArtifactFinder()
    @initD3()
    @initGraph()
    @initEventHandlers()

    @gui = new GUI()


  initArtifactFinder : ->

    @artifactFinder = new ArtifactFinder()
    $("#artifactFinder").append( @artifactFinder.domElement )

    #make first tab activate
    $("a[data-toggle=tab]").first().tab("show")


  initD3 : ->

    @svg = d3.select("#graph")
      .append("svg")
      .attr("WIDTH", WIDTH)
      .attr("HEIGHT", HEIGHT)
      .attr("pointer-events", "all")

    @hitbox = @svg.append("svg:rect")
          .attr("width", WIDTH)
          .attr("height", HEIGHT)
          .attr("fill", "white")
          .classed("hitbox", true)


  initGraph : ->

    @graphContainer = @svg.append("svg:g")

    @projectModel.get("graphs/0", this, (graphModel) ->

      @graph = new InteractiveGraph(@graphContainer, @svg, graphModel)

    )


  initEventHandlers : ->

    # add new node
    Hammer( $("svg")[0] ).on "tap", @addNode

    # drag artifact into graph
    $("body").on( "dragstart", "#artifactFinder .artifact-image", (e) -> e.preventDefault() )
    Hammer(document.body).on "dragend", "#artifactFinder .artifact-image", @addArtifact

    # change tool from toolbox
    processView = this
    $(".btn-group button").on "click", (event) -> processView.changeBehavior(this)

    # zooming
    $(".zoomSlider")
      .on("change", "input", @zoom)
      .on("click", ".plus", => @changeZoomSlider(0.1) )
      .on("click", ".minus", => @changeZoomSlider(-0.1) )

    do =>

      mouseDown = false

      Hammer(document.body)
        .on("touch", -> mouseDown = true; return )
        .on("release", -> mouseDown = false; return )

      $("body").on "mousewheel", (evt, delta, deltaX, deltaY) =>

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
    offset = $("svg").offset()

    if touch.pageX > offset.left and touch.pageY > offset.top

      id = artifact.data("id")
      artifact = @artifactFinder.getArtifact(id)
      $(artifact.domElement).find("img").addClass("nodeElement")

      @on "view:zooming", artifact.resize

      @addNode(evt, id, artifact)


  addNode : (evt, nodeId, artifact = null) =>

    offset = $("svg").offset()
    scaleValue = $(".zoomSlider input").val()

    x = event.gesture.touches[0].pageX - offset.left
    y = event.gesture.touches[0].pageY - offset.top

    x /= scaleValue
    y /= scaleValue

    @graph.addNode(x, y, nodeId, artifact)


  changeBehavior : (selectedTool) =>

    { graph, graphContainer } = @

    toolBox = $(".btn-group button")
    behavior = switch selectedTool

      when toolBox[0] then new DragBehavior(graph)
      when toolBox[1] then new ConnectBehavior(graph, graphContainer)
      when toolBox[2] then new DeleteBehavior(graph)
      when toolBox[3] then new DrawClusterBehavior(graph, graphContainer)

    graph.changeBehavior( behavior )


  zoom : (event) =>

    scaleValue = $(".zoomSlider input").val()

    @graphContainer.attr("transform", "scale( #{scaleValue} )") #"translate(" + d3.event.translate + ")
    @trigger("view:zooming")


  changeZoomSlider : (delta) ->

    $slider = $(".zoomSlider input")
    sliderValue = parseFloat($slider.val())
    $slider.val( sliderValue + delta )

    @zoom()