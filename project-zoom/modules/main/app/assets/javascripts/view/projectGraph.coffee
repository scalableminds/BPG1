### define
d3 : d3
lib/event_mixin : EventMixin
./process_view/graph : Graph
./process_view/node : Node
./process_view/behavior/connect_behavior : connectBehavior
./process_view/behavior/drag_behavior : dragBehavior
../component/project : Project
../component/layouter : Layouter
###

class ProjectGraph extends Graph

  constructor : (@container, @svg, @graphModel, @projects) ->

    @selectedTags = []
    @clusters = []

    EventMixin.extend(this)
    # @initArrowMarkers()
    # @initProjects()
    @initLayouter()

    @circles = @svg.append("svg:g").selectAll("circle")

    @projectNodes = @container.append("svg:g").selectAll("projectNode")

    super(@container, @graphModel)

    @currentBehavior = new dragBehavior(@)
    @currentBehavior.activate()


  addNode : (x, y, nodeId) =>

    id = nodeId ? @nodeId++

    node = new Node(
      x,
      y,
      id
    )

    @nodes.push node
    @drawNodes()

    node      # return node


  drawNodes : ->

    @projectNodes = @projectNodes.data(@projects, (data) -> data.id)


    g = @projectNodes.enter().append("svg:g")
    g.attr(
      transform: "translate(20,25)"
      )
    g.append("svg:circle")
      .attr(
        r: 30
        cx: (d) -> d.x
        cy: (d) -> d.y
        width: (d) -> d.width
        height: (d) -> d.height
        class: "projectImage"
      )
    headline = g.append("svg:text")
      .attr(
        x: (d) -> parseInt(d.x) + 50
        y: (d) -> parseInt(d.y) + 50
        class: "projectHeadline"
      )
      .text( (d) -> d.name )
    # g.append("svg:text") #tags!!!!

    @projectNodes.exit().remove()


  drawProjectGraph : () ->

    @drawNodes()


  drawEdges : () ->

  drawClusters : () ->










################# Venn: ##################


  drawVennCircles : ->

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


  updateVennDiagram : (checkbox) =>

    positions =
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
          position: positions[index]
          color: color[index]
          name: t
          id: t

        @clusters.push cluster

    @drawVennCircles()
    @arrangeProjectsInVenn()


  arrangeProjectsInVenn : () ->

    projectClusters =
      "left" : []
      "right" : []
      "bottom" : []
      "lr" : []
      "lb" : []
      "br" : []
      "middle" : []
      "no_cluster" : []

    for p in @projects
      selectedProjectTags = []
      for t in p.tags
        selectedProjectTags.push t.name if t.name in @selectedTags

      assignedCluster = @getAssignedVennCluster selectedProjectTags

      projectClusters[assignedCluster].push p.node

    @layouter.arrangeNodesInVenn(projectClusters)


  getAssignedVennCluster : (assignedTags) ->

    positions = []

    for c in assignedTags
      [cluster] = assignedTags.filter (c) -> c[0][0][0].id == "cluster_#{c}"
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

    # @layouter = new Layouter()


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






