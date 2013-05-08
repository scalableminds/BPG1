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

  SAMPLE_PROJECT_1 =
    name : "Test 1"
    tags : [
      {type : "project_partner", name : "SAP"},
      {type : "date", name : "2013"},
    ]
    img : null
    node : null

  SAMPLE_PROJECT_2 =
    name : "Test 1"
    tags : [
      {type : "project_partner", name : "Siemens"},
      {type : "date", name : "2013"},
      {type : "branch", name : "Diabetes"},
    ]
    img : null
    node : null

  SAMPLE_PROJECT_3 =
    name : "Test 1"
    tags : [
      {type : "project_partner", name : "Janssen"},
      {type : "branch", name : "Energy"},
    ]
    img : null
    node : null


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

    $(".checkbox-group input").on "click", (event) => @updateClusters(event.currentTarget)


  collectSelectedTags : ->

    @selectedTags = $("input[type=checkbox]:checked").map( ->
      @value
    ).get()


  updateClusters : (clickedCheckbox) ->

    @collectSelectedTags()
    clusterName = clickedCheckbox.value

    if clickedCheckbox.checked
      @drawCluster(clusterName)
    else @removeCluster(clusterName)


  drawCluster : (clusterName) ->

    switch @selectedTags.length
      when 1 then @venn1(clusterName)
      when 2 then @venn2(clusterName)
      when 3 then @venn3(clusterName)
      else @noVenn()


  removeCluster : (clusterName) ->
    d3.select("##{clusterName}").remove()


  venn1 : (clusterName) ->
    @drawCircle(300, 200, "steelblue", clusterName)
    @arrangeProjectsInClusters()

  venn2 : (clusterName) ->
    @drawCircle(550, 200, "yellow", clusterName)

  venn3 : (clusterName) ->
    @drawCircle(425, 400, "forestgreen", clusterName)

  noVenn : ->
    $("circle").each( ->
      @remove()
    )
    console.log "no Venn Diagramm possible."


  drawCircle : (cx, cy, color, name) ->

    circle = @svg.append("svg:circle")
      .attr({
        "r": 200,
        "cx": cx,
        "cy": cy,
        "fill": color,
        "fill-opacity": .5,
        "id": name,
      })

    @clusters.push circle
    # @drawLabelsForSelectedTags

  drawLabelsForSelectedTags : ->

    for t in @selectedTags
      label = @svg.append("svg:text")
      .attr({
        x: 100,
        y: 100,
        fill: "red",
      })
      .textContent = "now?"


  arrangeProjectsInClusters : ->

    # for project in @projects
    #   for tag in @selectedTags
        # if @hasProjectTag(project, tag)
          # console.log project
          # console.log tag
          # console.log "#######"
      # if tag in p.tags
      #   console.log "tagchen"

  hasProjectTag : (project, tag) ->
    for t in project.tags
      if JSON.stringify t == JSON.stringify tag
        break
        true
      else false






