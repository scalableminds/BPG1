### define
lib/event_mixin : EventMixin
d3 : d3
./projectGraph : ProjectGraph
../component/tagbar : Tagbar

###

class ProjectsOverviewView

  WIDTH = 960
  HEIGHT = 500
  MIDDLE_X = 325
  MIDDLE_Y = 325


  constructor : ->

    EventMixin.extend(this)
    @initTagbar()
    @initD3()
    @initGraph()
    @initEventHandlers()
    @initLayouter()


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
    @graph = new ProjectGraph(@graphContainer, @svg, @layouter)


  initEventHandlers : ->

    $(".checkbox-group input").on "click", (event) => @graph.updateClusters(event.currentTarget)






