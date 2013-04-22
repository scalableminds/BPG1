### define
lib/event_mixin : EventMixin
d3 : d3
hammer: Hammer
./process_view/interactive_graph : InteractiveGraph
./process_view/gui : GUI
./process_view/behavior/connect_nodes_behavior : ConnectNodesBehavior
./process_view/behavior/drag_node_behavior : DragNodeBehavior
./process_view/behavior/delete_node_behavior : DeleteNodeBehavior
../component/artifact_finder : ArtifactFinder
../component/artifact : Artifact
###

class ProcessView

  WIDTH = 960
  HEIGHT = 500
  time : null



  constructor : ->

    EventMixin.extend(this)
    @initArtifactFinder()
    @initD3()
    @initGraph()
    @initEventHandlers()

    @gui = new GUI()
    artifact = new Artifact @artifactFinder.SAMPLE_ARTIFACT, -> 64
    @graph.addForeignObject(artifact.domElement)
    @on "view:zooming", artifact.resize



  initArtifactFinder : ->

    @artifactFinder = new ArtifactFinder()
    $("#artifactFinder").append( @artifactFinder.domElement )


  initD3 : ->

    @svg = d3.select("#graph")
      .append("svg")
      .attr("WIDTH", WIDTH)
      .attr("HEIGHT", HEIGHT)
      .attr("pointer-events", "all")
      # .call(
      #   d3.behavior.zoom()
      #     .on("zoom", ( => @zoom()) )
      # )

    @hitbox = @svg.append("svg:rect")
          .attr("width", WIDTH)
          .attr("height", HEIGHT)
          .attr("fill", "white")
          .classed("hitbox", true)


  initGraph : ->

    @graphContainer = @svg.append("svg:g")

    @graph = new InteractiveGraph(@graphContainer, @svg)
    for i in [0..5]
      @graph.addNode(i*50, i*50)

    @graph.addEdge(0,1)
    @graph.addEdge(2,3)
    @graph.addEdge(4,3)


  initEventHandlers : ->


    Hammer( $("svg")[0] )
      .on("tap", ".hitbox", (event) =>
        offset = $("svg").offset()
        touch = event.gesture.touches[0]
        @graph.addNode(touch.pageX - offset.left, touch.pageY - offset.top)
      )

    processView = this
    $(".btn-group button").on "click", (event) -> processView.changeBehavior(this)

    $(".zoomSlider")
      .on("change", "input", @zoom)
      .on("click", ".plus", => @changeZoomSlider(0.1) )
      .on("click", ".minus", => @changeZoomSlider(-0.1) )


  changeBehavior : (selectedTool) =>

    { graph, graphContainer } = @

    toolBox = $(".btn-group button")
    behavior = switch selectedTool

      when toolBox[0] then new DragNodeBehavior()
      when toolBox[1] then new ConnectNodesBehavior(graph, graphContainer)
      when toolBox[2] then new DeleteNodeBehavior(graph)

    graph.changeBehavior( behavior )


  zoom : (event) =>

    scaleValue = $(".zoomSlider input").val()

    @graphContainer.attr("transform", "scale( #{scaleValue} )") #"translate(" + d3.event.translate + ")
    @trigger("view:zooming")
    console.log "zooming"


  changeZoomSlider : (delta) ->

    $slider = $(".zoomSlider input")
    sliderValue = parseFloat($slider.val())
    $slider.val( sliderValue + delta )

    @zoom()