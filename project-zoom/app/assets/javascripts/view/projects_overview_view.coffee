### define
lib/event_mixin : EventMixin
d3 : d3
./process_view/interactive_graph : InteractiveGraph
../component/tagbar : Tagbar
###

class ProjectsOverviewView

  WIDTH = 960
  HEIGHT = 500
  time : null

  SAMPLE_PROJECT : {
    name:"test1"
    tags : [
      {type :"project_partner", name : "SAP"}
      {type :"date", name : "2013"}
      {type :"topic", name : "Health"}
      {type :"topic", name : "Energy"}
    ]
  }


  constructor : ->

    @selectedTags = []
    @clusters = []

    EventMixin.extend(this)
    @initTagbar()
    @initD3()
    @initArrowMarkers()
    @initGraph()
    @initEventHandlers()


  initTagbar : ->

    @tagbar = new Tagbar()
    $("#tagbar").append( @tagbar.domElement )


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

    projectsOverviewView = this
    $(".checkbox-group input").on "click", (event) -> projectsOverviewView.drawClusters()


  collectSelectedTags : ->

    @selectedTags = $("input[type=checkbox]:checked").map( ->
      @value
    ).get()


  zoom : ->

    @graphContainer.attr("transform", "scale( #{d3.event.scale} )") #"translate(" + d3.event.translate + ")
    @trigger("view:zooming")
    console.log "zooming"


  drawClusters : ->

    for circ in @clusters
      console.log circ
      console.log @clusters
      # circ.parentNode.removeChild(circ)

    @collectSelectedTags()

    switch @selectedTags.length
      when 1 then @venn1()
      when 2 then @venn2()
      when 3 then @venn3()
      else @noVenn()


  venn1 : ->

    # @clusters.
    console.log @selectedTags[0]

    @drawCircle(300, 200, "steelblue")


  venn2 : ->

  venn3 : ->

  noVenn : ->
    console.log "no Venn Diagramm possible."


  drawCircle : (cx, cy, color) ->

    circle = @svg.append("svg:circle")
      .attr({
        "r": 200,
        "cx": cx,
        "cy": cy,
        "fill": color,
        "fill-opacity": .5,
      })

    @clusters.push circle




