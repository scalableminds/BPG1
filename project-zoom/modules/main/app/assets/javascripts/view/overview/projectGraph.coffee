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
    @scale_value = 1.0

    EventMixin.extend(this)
    @initLayouter()

    @circles = @svg.append("svg:g").selectAll("circle")

    @projectNodes = @graphContainer.append("svg:g").selectAll("projectNode")

    @currentBehavior = new DragBehavior(@)
    @currentBehavior.activate()


  drawProjects : (scale_value = @scale_value, projects = @projects) ->

    @scale_value = scale_value
    names = []
    layouter = @layouter
    @projectNodes = @projectNodes.data(projects, (data) -> data.id)

    start_x = start_y = x = y = 20
    margin_x = 30
    margin_y = 70
    nodeWidth = 100
    svgWidth = parseInt @svg.attr("width") / scale_value
    next_line = parseInt(svgWidth / (margin_x + nodeWidth))

    g = @projectNodes.enter().append("svg:g")
    g.append("svg:image")
      .attr(
        class: "projectImage"
        x: (d, i) -> margin_x + (i % next_line) * (nodeWidth + margin_x)
        y: (d, i) -> start_y + parseInt(i / next_line) * (nodeWidth + margin_y)
        width: (d) -> d.width
        height: (d) -> d.height
        "xlink:href": (d) -> d.image
      )
    headline = g.append("svg:text")
      .attr(
        class: "projectHeadline"
        x: (d, i) -> margin_x + (i % next_line) * (nodeWidth + margin_x)
        y: (d, i) -> start_y + parseInt(i / next_line) * (nodeWidth + margin_y) + 120
        workaround: (d) -> names.push d.name; return ""
      )
    g.attr(
      id: (d) -> d.id
      "data-tags": (d)-> d.tags.toString()
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

    # @drawVennCircles()
    @onlyShowTagged()
    # @arrangeProjectsInVenn() # tagged = @projectNodes!!!!


  onlyShowTagged : ->

    tagged = []
    if @selectedTags.length isnt 0
      for p in @projects
        if _.intersection(p.tags, @selectedTags).length isnt 0
          tagged.push p

      @drawProjects(@scale_value, [])
      @drawProjects(@scale_value, tagged)
    else
      @drawProjects(@scale_value, [])
      @drawProjects()


  arrangeProjectsInVenn : () ->

    projectClusters =
      "left" : []
      "right" : []
      "bottom" : []
      "lr" : []
      "lb" : []
      "br" : []
      "middle" : []


    for p in @projectNodes[0]
      projectTags = _.flatten [$(p).data("tags").toString()]
      selectedProjectTags = _.intersection(projectTags, @selectedTags)

      assignedCluster = @getAssignedVennCluster selectedProjectTags
      projectClusters[assignedCluster].push $("##{p.id}")

    @layouter.arrangeNodesInVenn(projectClusters)


  getAssignedVennCluster : (assignedTags) ->

    positions = []

    for c in assignedTags
      positions.push $("#cluster_#{c}").data("pos")

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


  initLayouter : ->

    @layouter = new Layouter()






