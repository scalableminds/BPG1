### define
d3 : d3
lib/event_mixin : EventMixin
./behavior/behavior : Behavior
./behavior/connect_behavior : ConnectBehavior
./behavior/drag_behavior : DragBehavior
./behavior/delete_behavior : DeleteBehavior
../../component/layouter : Layouter
###

class ProjectGraph

  constructor : (@graphContainer, @svg, @projects) ->

    @selectedTags = []
    @clusters = []

    EventMixin.extend(this)
    @initLayouter()

    @circles = @svg.append("svg:g").selectAll("circle")

    @projectNodes = @graphContainer.append("svg:g").selectAll("projectNode")

    @currentBehavior = new DragBehavior(@)
    @currentBehavior.activate()


  drawProjects : (projects = @projects) ->

    names = []
    layouter = @layouter
    @projectNodes = @projectNodes.data(projects, (data) -> data.id)

    g = @projectNodes.enter().append("svg:g")
    g.append("svg:image")
      .attr(
        class: "projectImage"
        x: (d) -> d.x
        y: (d) -> d.y
        width: (d) -> d.width
        height: (d) -> d.height
        "xlink:href": (d) -> d.image
      )
    headline = g.append("svg:text")
      .attr(
        class: "projectHeadline"
        x: (d) -> parseInt(d.x)
        y: (d) -> parseInt(d.y) + 120
        workaround: (d) -> names.push d.name; return ""
      )
    g.attr(
      id: (d) -> d.id
      )

    for h, i in headline[0]
      @layouter.textWrap(h, names[i], 120)

    # g.append("svg:text") #tags!!!!

    @projectNodes.exit().remove()


  changeBehavior : (behavior) ->

    @currentBehavior.deactivate()
    @currentBehavior = behavior
    @currentBehavior.activate()










################# Venn: ##################


  drawVennCircles : ->

    @circles = @circles.data(@clusters, (data) -> data.id)
    g = @circles.enter().append("svg:g")
    g.append("svg:circle")
      .attr(
        r: 200
        cx: (d) -> d.position[0]
        cy: (d) -> d.position[1]
        id: (d) -> "cluster_#{d.name}"
        fill: (d) -> d.color
        "fill-opacity": .5
        "data-pos": (d) -> d.data_pos
      )
    g.append("svg:text")
      .attr(
        id: "text1"
        x: (d) -> d.position[2]
        y: (d) -> d.position[3]
        id: (d) -> "label_#{d.name}"
        class: "label"
      )
      .text( (d) -> d.name )

    @circles.exit().remove()


  updateVennDiagram : (checkbox) =>

    positions =
      0: [[300, 200, 250, 50], "left"]
      1: [[550, 200, 550, 50], "right"]
      2: [[425, 400, 425, 575], "bottom"]

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
          position: positions[index][0]
          color: color[index]
          name: t
          id: t
          data_pos: positions[index][1]

        @clusters.push cluster

    @drawVennCircles()
    @onlyShowTagged()
    @arrangeProjectsInVenn() # tagged = @projectNodes!!!!


  onlyShowTagged : ->

    tagged = []
    for p in @projects
      if _.intersection(p.tags, @selectedTags).length isnt 0
        tagged.push p

    @drawProjects tagged


  arrangeProjectsInVenn : (tagged) ->

    projectClusters =
      "left" : []
      "right" : []
      "bottom" : []
      "lr" : []
      "lb" : []
      "br" : []
      "middle" : []
      "no_cluster" : []

    for p in tagged
      selectedProjectTags = []
      for t in p.tags
        selectedProjectTags.push t if t in @selectedTags

      assignedCluster = @getAssignedVennCluster selectedProjectTags

      projectClusters[assignedCluster].push p.node

    @layouter.arrangeNodesInVenn(projectClusters)


  getAssignedVennCluster : (assignedTags) ->

    positions = []

    for c in assignedTags
      [cluster] = @circles.filter (ci) -> ci[0][0][0].id == "cluster_#{c}"
      positions.push $(cluster).data("pos")

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


  # changeBehavior : (behavior) ->

  #   @currentBehavior.deactivate()
  #   @currentBehavior = behavior
  #   @currentBehavior.activate()



  initLayouter : ->

    # alert @snap(14, 10)
    # alert @snap(16, 10)

    @layouter = new Layouter()


  # initProjects : ->

  #   for p in SAMPLE_PROJECTS
  #     project = new Project(p)

  #     @projects.push project

  #   pos_x = 20
  #   pos_y = 20

  #   for p in @projects
  #     node = @addNode(pos_x, pos_y)

  #     p.setNode node

  #     pos_x += 70
  #     pos_y += 70


  # initArrowMarkers : ->

  #   # define arrow markers for graph edges
  #   @svg.append("svg:defs")
  #     .append("svg:marker")
  #       .attr("id", "end-arrow")
  #       .attr("viewBox", "0 -5 10 10")
  #       .attr("refX", 6)
  #       .attr("markerWidth", 3)
  #       .attr("markerHeight", 3)
  #       .attr("orient", "auto")
  #     .append("svg:path")
  #       .attr("d", "M0,-5L10,0L0,5")
  #       .attr("fill", "#000")

  #   @svg.append("svg:defs")
  #     .append("svg:marker")
  #       .attr("id", "start-arrow")
  #       .attr("viewBox", "0 -5 10 10")
  #       .attr("refX", 4)
  #       .attr("markerWidth", 3)
  #       .attr("markerHeight", 3)
  #       .attr("orient", "auto")
  #     .append("svg:path")
  #       .attr("d", "M10,-5L0,0L10,5")
  #       .attr("fill", "#000")

  snap : (value, gridSize, roundFunction) ->

    roundFunction = Math.round  if roundFunction is `undefined`
    gridSize * roundFunction(value / gridSize)






