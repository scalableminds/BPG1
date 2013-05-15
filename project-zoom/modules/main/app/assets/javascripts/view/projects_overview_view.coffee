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
    name : "Test 2"
    tags : [
      {type : "project_partner", name : "Siemens"},
      {type : "date", name : "2013"},
      {type : "branch", name : "Diabetes"},
    ]
    img : null
    node : null

  SAMPLE_PROJECT_3 =
    name : "Test 3"
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
    console.log @graph
    pos_x = 0
    pos_y = 0
    for project in @projects

      nodeContainer = @graph.addNode(pos_x, pos_y)
      svgNodeContainer = nodeContainer[nodeContainer.length - 1].parentNode.childNodes
      node = svgNodeContainer[svgNodeContainer.length - 1].childNodes[0]

      project.node = node

      pos_x += 50
      pos_y += 50


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
    tagName = clickedCheckbox.value

    if clickedCheckbox.checked
      @drawCluster(tagName)
    else @removeCluster(tagName)

    @arrangeProjectsInClusters(tagName)


  drawCluster : (name) ->
    switch @selectedTags.length
      when 1 then @venn1(name)
      when 2 then @venn2(name)
      when 3 then @venn3(name)
      else @noVenn()


  removeCluster : (name) ->
    if d3.select("#cluster_#{name}")?
      d3.select("#cluster_#{name}").remove()


  venn1 : (name) ->
    @drawCircle(300, 200, "steelblue", name)

  venn2 : (name) ->
    @drawCircle(550, 200, "yellow", name)

  venn3 : (name) ->
    @drawCircle(425, 400, "forestgreen", name)

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
        "id": "cluster_#{name}",
      })

    @clusters.push circle
    # @drawLabelsForSelectedTags

  # drawLabelsForSelectedTags : ->
  #   for t in @selectedTags
  #     label = @svg.append("svg:text")
  #     .attr({
  #       x: 100,
  #       y: 100,
  #       fill: "red",
  #     })
  #     .textContent = "now?"


  arrangeProjectsInClusters : (tagName) ->
    for project in @projects
      selectedProjectTags = []

      if @hasProjectTag(project, tagName)
        selectedProjectTags.push tagName

      @updateNode(project.node, selectedProjectTags)


  hasProjectTag : (project, tag) ->
    for t in project.tags
      if t.name == tag
        return true
      else return false


  updateNode : (projectNode, selectedProjectTags) ->
    pos_x = 0
    pos_y = 0

    for t in selectedProjectTags
      c = d3.select("#cluster_#{t}")
      pos_x += parseInt c.attr("cx")
      pos_y += parseInt c.attr("cy")

      projectNode.x.baseVal.value = pos_x
      projectNode.y.baseVal.value = pos_y







