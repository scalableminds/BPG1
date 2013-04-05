### define
lib/event_mixin : EventMixin
d3 : d3
./interactive_graph : InteractiveGraph
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
    @initArrowMarkers()
    @initGraph()
    @initEventHandlers()

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
      .call(
        d3.behavior.zoom()
          .on("zoom", ( => @zoom()) )
      )

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


  initArrowMarkers : ->

    # define arrow markers for graph edges
    @svg.append("svg:defs")
      .append("svg:marker")
        .attr("id", "end-arrow")
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 6)
        .attr("markerWidth", 3)
        .attr("markerHeight", 3)
        .attr("orient", "auto")
      .append("svg:path")
        .attr("d", "M0,-5L10,0L0,5")
        .attr("fill", "#000")

    @svg.append("svg:defs")
      .append("svg:marker")
        .attr("id", "start-arrow")
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 4)
        .attr("markerWidth", 3)
        .attr("markerHeight", 3)
        .attr("orient", "auto")
      .append("svg:path")
        .attr("d", "M10,-5L0,0L10,5")
        .attr("fill", "#000")


  initEventHandlers : ->

    graphContainer = @graphContainer[0][0]
    @hitbox.on "click", => @graph.addNode(d3.mouse(graphContainer)[0], d3.mouse(graphContainer)[1])


  zoom : ->

    @graphContainer.attr("transform", "scale( #{d3.event.scale} )") #"translate(" + d3.event.translate + ")
    @trigger("view:zooming")
    console.log "zooming"