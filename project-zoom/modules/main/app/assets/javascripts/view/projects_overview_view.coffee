### define
lib/event_mixin : EventMixin
d3 : d3
./process_view/interactive_graph : InteractiveGraph
../component/tagbar : Tagbar
../component/project : Project
../component/layouter : Layouter
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

  SAMPLE_PROJECTS = [SAMPLE_PROJECT_1, SAMPLE_PROJECT_2, SAMPLE_PROJECT_3]


  constructor : ->
    @selectedTags = []
    @clusters = []
    @projects = []

    EventMixin.extend(this)
    @initTagbar()
    @initD3()
    @initArrowMarkers()
    @initProjects()
    @initGraph()
    @initEventHandlers()
    @initLayouter()


  initLayouter : ->
    @layouter = new Layouter()


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

    # @makePackLayout()


  initGraph : ->
    @graphContainer = @svg.append("svg:g")
    @graph = new InteractiveGraph(@graphContainer, @svg)

    pos_x = 20
    pos_y = 20

    for p in @projects
      nodeContainer = @graph.addNode(pos_x, pos_y)
      svgNodeContainer = nodeContainer[nodeContainer.length - 1].parentNode.childNodes
      node = svgNodeContainer[svgNodeContainer.length - 1].childNodes[0]

      p.setNode node

      pos_x += 70
      pos_y += 70


  initProjects : ->
    for p in SAMPLE_PROJECTS
      project = new Project(p.name)

      for t in p.tags
        project.addTag t

      project.setImage p.img

      @projects.push project


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
    # @layouter.drawVenn(@selectedTags, @projects)
    if clickedCheckbox.checked
      @drawCluster(tagName)

    else if @selectedTags.length == 3     # venn diagramm possible again
      @venn1 @selectedTags[0]
      @venn2 @selectedTags[1]
      @venn3 @selectedTags[2]

    else if @selectedTags < 3
      @removeCluster(tagName)

    else
      console.log "still no venn diagramm possible"

    @arrangeProjectsInClusters()


  drawCluster : (name) ->
    # console.log name
    switch @selectedTags.length
      when 1 then @venn1(name)
      when 2 then @venn2(name)
      when 3 then @venn3(name)
      else @noVenn()


  removeCluster : (name) ->
    if d3.select("#cluster_#{name}")?
      d3.select("#cluster_#{name}").remove()

    @clusters.filter (c) -> c[0][0].id.toString() isnt "cluster_#{name}"


  venn1 : (name) ->
    @drawCircle("left", "steelblue", name)

  venn2 : (name) ->
    @drawCircle("right", "yellow", name)

  venn3 : (name) ->
    @drawCircle("bottom", "forestgreen", name)

  noVenn : ->
    $("circle").each( ->
      @remove()
    )
    console.log "no Venn Diagramm possible."


  drawCircle : (location, color, name) ->
    circle = @svg.append("svg:circle")
    # console.log circle

    switch location
      when "left" then position = [300, 200]
      when "right" then position = [550, 200]
      when "bottom" then position = [425, 400]

    circle.attr({
        "r": 200,
        "cx": position[0],
        "cy": position[1],
        "fill": color,
        "fill-opacity": .5,
        "id": "cluster_#{name}",
    })
    circle.pos = location

    @clusters.push circle

    # @drawLabelsForSelectedTags


################## To Be Refactored: ###################

  # drawLabelsForSelectedTags : ->
  #   for t in @selectedTags
  #     label = @svg.append("svg:text")
  #     .attr({
  #       x: 100,
  #       y: 100,
  #       fill: "red",
  #     })
  #     .textContent = "now?"



  arrangeProjectsInClusters : () ->
    for p in @projects
      selectedProjectTags = []
      for t in p.tags
        selectedProjectTags.push t.name if t.name in @selectedTags

      @updateNode(p, selectedProjectTags)


  hasProjectTag : (project, tag) ->

    for t in project.tags
      if t.name == tag
        return true
      else return false


  # oneTag : (positions, projectNode) ->

  #   switch positions[0]
  #     when "left" then @left(projectNode)
  #     when "right" then @right(projectNode)
  #     when "bottom" then @bottom(projectNode)

  # two : (positions, projectNode) ->
  #   # console.log "two"
  #   switch positions
  #     when ["left", "right"] or ["right", "left"] then @leftRight(projectNode)
  #     when ["left", "bottom"] or ["bottom", "left"] then @leftBottom(projectNode)
  #     when ["bottom", "right"] or ["right", "bottom"] then @bottomRight(projectNode)

  # three : (positions, projectNode) ->
  #   # console.log "three"
  #   projectNode.x.baseVal.value += 400
  #   projectNode.x.baseVal.value += 200

  # left : (projectNode) ->
  #   projectNode.x.baseVal.value += 300

  # right : (projectNode) ->
  #   projectNode.x.baseVal.value += 550

  # bottom : (projectNode) ->
  #   projectNode.x.baseVal.value += 425
  #   projectNode.y.baseVal.value += 400

  # leftRight : (projectNode) ->
  #   projectNode.x.baseVal.value += 425

  # bottomRight : (projectNode) ->
  #   projectNode.x.baseVal.value += 450
  #   projectNode.y.baseVal.value += 370

  # leftBottom : (projectNode) ->
  #   projectNode.x.baseVal.value += 350
  #   projectNode.y.baseVal.value += 370


  updateNode : (project, selectedProjectTags) ->
    window.debug = @clusters

    if selectedProjectTags.length > 3
      console.log "no venn anyway"

    else if selectedProjectTags.length == 0
      console.log "no venn anyway --> 0"

    else if selectedProjectTags.length == 1
      [cluster] = @clusters.filter (c) -> c[0][0].id == "cluster_#{selectedProjectTags[0]}"
      x = cluster[0][0].cx.baseVal.value
      y = cluster[0][0].cy.baseVal.value
      project.moveNode(x, y)

    else if selectedProjectTags.length == 2
      [cluster1] = @clusters.filter (c) -> c[0][0].id == "cluster_#{selectedProjectTags[0]}"
      [cluster2] = @clusters.filter (c) -> c[0][0].id == "cluster_#{selectedProjectTags[1]}"

      x1 = cluster1[0][0].cx.baseVal.value
      x2 = cluster2[0][0].cx.baseVal.value
      y1 = cluster1[0][0].cy.baseVal.value
      y2 = cluster2[0][0].cy.baseVal.value

      x = x1+(x2-x1)/2 ? x1 < x2 : x2+(x1-x2)/2
      y = y1+(y2-y1)/2 ? y1 < y2 : y2+(y1-y2)/2
      console.log "2!!!!!!!!!!!!!!!!!!!!!!!!!!"
      console.log x
      project.moveNode(x, y)

    else

    # else if selectedProjectTags.length == 2

    # else if selectedProjectTags.length == 3







