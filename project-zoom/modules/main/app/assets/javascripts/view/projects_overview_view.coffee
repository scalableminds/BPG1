### define
lib/event_mixin : EventMixin
d3 : d3
./projectGraph : ProjectGraph
../component/tagbar : Tagbar
app : app
jquery : $
###

class ProjectsOverviewView

  WIDTH = 960
  HEIGHT = 500
  MIDDLE_X = 325
  MIDDLE_Y = 325


  constructor : ->

    EventMixin.extend(this)

    @initEventHandlers()

    @initTagbar()
    @initD3()
    @initGraph()


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

    @projectNodes = []

    app.model.projectGraph.get("nodes").forEach( (projectNode) =>

      node =
        id: projectNode.get("id")
        name: projectNode.get("name")
        season: projectNode.get("season")
        year: projectNode.get("year")
        length: projectNode.get("length")
        participants: projectNode.get("participants")
        x: 100
        y: 200

      @projectNodes.push node
    )

    @graph.drawProjectGraph @projectNodes


  initEventHandlers : ->

    $(".checkbox-group input").on "click", (event) => @graph.updateVennDiagram(event.currentTarget)






