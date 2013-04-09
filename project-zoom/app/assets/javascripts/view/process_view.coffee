### define
lib/event_mixin : EventMixin
d3 : d3
./process_view/interactive_graph : InteractiveGraph
./process_view/gui : GUI
./process_view/behavior/connect_nodes_behavior : ConnectNodesBehavior
./process_view/behavior/drag_node_behavior : DragNodeBehavior
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


  initGraph : ->

    @graphContainer = @svg.append("svg:g")

    @graph = new InteractiveGraph(@graphContainer, @svg)
    for i in [0..5]
      @graph.addNode(i*50, i*50)

    @graph.addEdge(0,1)
    @graph.addEdge(2,3)
    @graph.addEdge(4,3)


  initEventHandlers : ->

    graphContainer = @graphContainer[0][0]
    @hitbox.on "click", => @graph.addNode(d3.mouse(graphContainer)[0], d3.mouse(graphContainer)[1])

    processView = this
    $(".navbar li").on "click", (event) -> processView.changeBehavior(this)


  changeBehavior : (selectedTool) =>

    { graphContainer, svg, graph } = @

    toolBox = $(".navbar li")
    behavior = switch selectedTool

      when toolBox[0] then new DragNodeBehavior(svg, graph)
      when toolBox[1] then new ConnectNodesBehavior(svg, graph, graphContainer)

    graph.changeBehavior( behavior )


  zoom : ->

    @graphContainer.attr("transform", "scale( #{d3.event.scale} )") #"translate(" + d3.event.translate + ")
    @trigger("view:zooming")
    console.log "zooming"