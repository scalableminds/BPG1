### define
d3 : d3
lib/event_mixin : EventMixin
./graph : Graph
./behavior/connect_behavior : connectBehavior
./behavior/drag_behavior : dragBehavior
###

class InteractiveGraph extends Graph

  constructor : (@graphModel) ->

    EventMixin.extend(this)

    @domElement = d3.select("svg")
    @graphContainer = @domElement.append("svg:g")

    @initArrowMarkers()
    @initCallouts()

    @currentBehavior = new dragBehavior(@)
    @currentBehavior.activate()

    super(@graphContainer, @graphModel)


  initArrowMarkers : ->

    # define arrow markers for graph edges
    @domElement.append("svg:defs")
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

    @domElement.append("svg:defs")
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


  initCallouts : ->

    @domElement.append("svg:defs")
      .append("svg:g")
        .attr(
          id: "comment-callout"
          class: "comment-callout"
        )
        .append("svg:path")
          .attr(
            d: "M 91.327094,24.650308 29.698196,25.08895 c -3.806634,0.02708 -10.252469,1.001177 -11.623829,6.360221 -1.58407,7.047529 -2.111911,38.764592 0.43877,43.644832 1.880362,3.93612 3.626479,5.248771 5.482699,6.360056 5.498796,2.64158 31.143682,2.193113 31.143682,2.193113 L 31.014321,102.0702 66.763348,83.427882 l 26.537645,1.8e-4 c 3.723799,0 7.129077,-2.341813 8.334117,-7.45707 2.09811,-9.288053 2.50653,-35.338056 0.43866,-41.451347 -1.9596,-5.58538 -5.252949,-9.90844 -10.746676,-9.869337 z"
          )
          .style(
            fill: "white";
            stroke: "black";
            "stroke-width": 2;
          )

  changeBehavior : (behavior) ->

    @currentBehavior.deactivate()
    @currentBehavior = behavior
    @currentBehavior.activate()











