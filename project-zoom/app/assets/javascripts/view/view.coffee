### define
lib/event_mixin : EventMixin
d3 : d3
./interactive_graph : InteractiveGraph
###

class View

  WIDTH = 960
  HEIGHT = 500



  constructor : ->

    EventMixin.extend(this)

    @initD3()
    @initArrowMarkers()
    @initGraph()
    @initEventHandlers()


  initD3 : ->

    @svg = d3.select("body")
      .append("svg")
      .attr("WIDTH", WIDTH)
      .attr("HEIGHT", HEIGHT)
      .attr("pointer-events", "all")

    @hitbox = @svg.append("svg:rect")
          .attr("width", WIDTH)
          .attr("height", HEIGHT)
          .attr("fill", "white")
          .call(
            d3.behavior.zoom()
              .on("zoom", ( => @zoom()) )
          )


  initGraph : ->

    graphContainer = @svg.append("svg:g")

    @graph = new InteractiveGraph(graphContainer, @hitbox, @svg)
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

    @hitbox.on "click", => @graph.addNode(d3.event.offsetX, d3.event.offsetY)
    @hitbox.on "mousemove", @graph.drawDragLine


  zoom : ->
    console.log "zooming"