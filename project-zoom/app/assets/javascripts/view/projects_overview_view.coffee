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

  SAMPLE_PROJECT_1 = {
    name : "Test 1",
    tags : [
      {type :"project_partner", name : "SAP"},
      {type :"date", name : "2013"},
      {type :"branch", name : "Health"},
      {type :"branch", name : "Energy"},
    ],
    img : null,
    node : null,
  }

  SAMPLE_PROJECT_2 = {
    name : "Test 2",
    tags : [
      {type :"project_partner", name : "SAP"},
      {type :"date", name : "2013"},
      {type :"branch", name : "Health"},
      {type :"branch", name : "Energy"},
    ],
    img : null,
    node : null,
  }

  SAMPLE_PROJECT_3 = {
    name : "Test 3",
    tags : [
      {type :"project_partner", name : "SAP"},
      {type :"date", name : "2013"},
      {type :"branch", name : "Health"},
      {type :"branch", name : "Energy"},
    ],
    img : null,
    node : null,
  }


  constructor : ->

    @selectedTags = []
    @clusters = []
    @projects = []

    EventMixin.extend(this)
    @initTagbar()
    @initD3()
    @initArrowMarkers()
    @initGraph()
    @initEventHandlers()


  initTagbar : ->

    @tagbar = new Tagbar()
    $("#tagbar").append( @tagbar.domElement )
    @tagbar.populateTagForm()


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

    @projects.push SAMPLE_PROJECT_1
    @projects.push SAMPLE_PROJECT_2
    @projects.push SAMPLE_PROJECT_3

    @graphContainer = @svg.append("svg:g")

    @graph = new InteractiveGraph(@graphContainer, @svg)
    pos_x = 0
    pos_y = 0
    for project in @projects
      node = @graph.addNode(pos_x, pos_y)
      pos_x += 50
      pos_y += 50
      project.node = node

    # for i in [0..5]
    #   @graph.addNode(i*50, i*50)

    # @graph.addEdge(0,1)
    # @graph.addEdge(2,3)
    # @graph.addEdge(4,3)


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

    @graphContainer.attr("transform", "scale( #{d3.event.scale} )")
    @trigger("view:zooming")
    console.log "zooming"


  drawClusters : ->

    $("circle").each( ->
      @remove()
    )

    @collectSelectedTags()

    switch @selectedTags.length
      when 1 then @venn1()
      when 2 then @venn2()
      when 3 then @venn3()
      else @noVenn()


  venn1 : ->
    @drawCircle(300, 200, "steelblue")

  venn2 : ->
    @drawCircle(300, 200, "steelblue")
    @drawCircle(550, 200, "yellow")

  venn3 : ->
    @drawCircle(300, 200, "steelblue")
    @drawCircle(550, 200, "yellow")
    @drawCircle(425, 400, "forestgreen")

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
    # @drawLabelsForSelectedTags

  drawLabelsForSelectedTags : ->
    for t in @selectedTags
      label = @svg.append("svg:text")
      label.attr({
        x: 100,
        y: 100,
        fill: "red",
        })
      label.textContent = "now?"

  # arrangeProjectsInClusters : ->
  #   for tag in @selectedTags








