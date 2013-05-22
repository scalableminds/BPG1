### define
lib/event_mixin : EventMixin
d3 : d3
./projectGraph : ProjectGraph
../component/tagbar : Tagbar
../component/project : Project

###

class ProjectsOverviewView

  WIDTH = 960
  HEIGHT = 500
  MIDDLE_X = 325
  MIDDLE_Y = 325

  SAMPLE_PROJECT_1 =
    name : "Test 1"
    tags : [
      {type : "project_partner", name : "SAP"},
      {type : "date", name : "2013"},
      {type : "branch", name : "Health"},
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
    img : "http://cdn.arstechnica.net/wp-content/uploads/2012/10/06_Place_20773_1_Mis.jpg"
    node : null

  SAMPLE_PROJECT_3 =
    name : "Test 3"
    tags : [
      {type : "project_partner", name : "Janssen"},
      {type : "branch", name : "Energy"},
    ]
    img : "http://www.thinkstockphotos.com/CMS/StaticContent/WhyThinkstockImages/Best_Images.jpg"
    node : null

  SAMPLE_PROJECTS = [SAMPLE_PROJECT_1, SAMPLE_PROJECT_2, SAMPLE_PROJECT_3]


  constructor : ->
    @selectedTags = []
    @clusters = []
    @projects = []

    EventMixin.extend(this)
    @initTagbar()
    @initD3()
    @initProjects()
    @initGraph()
    @initEventHandlers()
    # @initLayouter()


  initLayouter : ->

    @layouter = new Layouter()


  initTagbar : ->

    @tagbar = new Tagbar()
    $("#tagbar").append( @tagbar.domElement )


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

    @graphContainer = @svg.append("svg:g")
    @graph = new ProjectGraph(@graphContainer, @svg)

    pos_x = 20
    pos_y = 20

    @circles = @svg.append("svg:g").selectAll("circle")

    for p in @projects
      nodeContainer = @graph.addNode(pos_x, pos_y)
      node = nodeContainer[0].parentNode.lastChild

      p.setNode node

      pos_x += 70
      pos_y += 70


  initProjects : ->

    for p in SAMPLE_PROJECTS
      project = new Project(p)

      @projects.push project


  initEventHandlers : ->

    $(".checkbox-group input").on "click", (event) => @updateClusters(event.currentTarget)


######################### Drawing: #########################


  drawClusters : ->

    @circles = @circles.data(@clusters, (data) -> data.id)
    g = @circles.enter().append("svg:g")
    g.append("svg:text")
      .attr(
        id: "text1"
        x: (d) -> d.position[2]
        y: (d) -> d.position[3]
        id: (d) -> "label_#{d.name}"
        class: "label"
      )
      .text( (d) -> d.name )
    g.append("svg:circle")
      .attr(
        r: 200
        cx: (d) -> d.position[0]
        cy: (d) -> d.position[1]
        id: (d) -> "cluster_#{d.name}"
        fill: (d) -> d.color
        "fill-opacity": .5
        "data-pos": location
      )

    @circles.exit().remove()


######################### Arranging: #########################

  updateClusters : (checkbox) =>

    location =
      0: [300, 200, 250, 50]
      1: [550, 200, 550, 50]
      2: [425, 400, 425, 575]

    color =
      0: "steelblue"
      1: "yellow"
      2: "forestgreen"

    if $(checkbox).is(":checked")
      @selectedTags.push checkbox.value
    else
      @selectedTags = (tag for tag in @selectedTags when tag isnt checkbox.value)

    @clusters = []

    if @selectedTags.length <= 3
      for t, index in @selectedTags
        cluster =
          position: location[index]
          color: color[index]
          name: t
          id: t

        @clusters.push cluster

    @drawClusters()

    @arrangeProjectsInClusters()


  arrangeProjectsInClusters : () ->
    left = right = bottom = lr = lb = br = middle = no_cluster = []

    for p in @projects
      selectedProjectTags = []
      for t in p.tags
        selectedProjectTags.push t.name if t.name in @selectedTags

      position = @getPosition selectedProjectTags

      switch position
        when "left" then left.push p.node
        when "right" then right.push p.node
        when "bottom" then bottom.push p.node
        when "lr" then lr.push p.node
        when "lb" then lb.push p.node
        when "br" then br.push p.node
        when "middle" then middle.push p.node
        when "no_cluster" then no_cluster.push p.node

    all = [left, right, bottom, middle, lb, lr, br, no_cluster]

    # @resizeCircles all
    # @arrangeProjects all

    # @layouter.arrangeInSquare()
    # @layouter.resizeCircle()
      # @updateNode(p, selectedProjectTags)


  resizeAllCircles : () ->

  arrangeAllProjects : () ->

######################### Calculating: #########################

  collectSelectedTags : ->
    @selectedTags = $("input[type=checkbox]:checked").map( ->
      @value
    ).get()


  getPosition : (clusters) ->
    positions = []

    for c in clusters
      [cluster] = @clusters.filter (c) -> c[0][0][0].id == "cluster_#{c}"
      positions.push cluster.pos

    if "left" in positions
      if "right" in positions
        if "bottom" in positions
          return "middle"
        else return "lr"
      else if "bottom" in positions
        return "lb"
      else return "left"
    else if "right" in positions
      if "bottom" in positions
        return "br"
      else return "right"
    else if "bottom" in positions
      return "bottom"
    else return "no_cluster"


  hasProjectTag : (project, tag) ->

    for t in project.tags
      if t.name == tag
        return true
      else return false


######################### Deprecated: #########################

  # updateNode : (project, selectedProjectTags) ->
  #   if selectedProjectTags.length > 3
  #     console.log "no venn anyway"

  #   else if selectedProjectTags.length == 0
  #     console.log "no venn anyway --> 0"

  #   else if selectedProjectTags.length == 1
  #     [cluster] = @clusters.filter (c) -> c[0][0][0].id == "cluster_#{selectedProjectTags[0]}"
  #     x = cluster[0][0][0].cx.baseVal.value
  #     y = cluster[0][0][0].cy.baseVal.value
  #     project.moveNode(x, y)

  #   else if selectedProjectTags.length == 2
  #     [cluster1] = @clusters.filter (c) -> c[0][0][0].id == "cluster_#{selectedProjectTags[0]}"
  #     [cluster2] = @clusters.filter (c) -> c[0][0][0].id == "cluster_#{selectedProjectTags[1]}"

  #     x1 = cluster1[0][0][0].cx.baseVal.value
  #     x2 = cluster2[0][0][0].cx.baseVal.value
  #     y1 = cluster1[0][0][0].cy.baseVal.value
  #     y2 = cluster2[0][0][0].cy.baseVal.value

  #     x = x1+(x2-x1)/2 ? x1 < x2 : x2+(x1-x2)/2
  #     y = y1+(y2-y1)/2 ? y1 < y2 : y2+(y1-y2)/2
  #     console.log "2!!!!!!!!!!!!!!!!!!!!!!!!!!"
  #     console.log x
  #     project.moveNode(x, y)

  #   else
  #     x = MIDDLE_X
  #     y = MIDDLE_Y
  #     project.moveNode(x, y)


  # arrangeProjectsInClusters : () ->
  #   for p in @projects
  #     selectedProjectTags = []
  #     for t in p.tags
  #       selectedProjectTags.push t.name if t.name in @selectedTags

  #     @updateNode(p, selectedProjectTags)





