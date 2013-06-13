### define
hammer : Hammer
./behavior : Behavior
../cluster : Cluster
lib/data_item : DataItem
app : app
###

class DrawClusterBehavior extends Behavior

  constructor : ( @graph, @$el, @type ) ->

    @throttledDragMove = _.throttle(@dragMove, 50)
    if @graph.$svgEl.find(".preview").length == 0
      @preview = @graph.graphContainer.insert("svg:path",":first-child") #prepend for proper zOrdering
      @preview
        .attr("class", "hide preview cluster")
    else
      @preview = @graph.d3Element.select(".preview")

    super(@graph)


  activate : ->

    @hammerContext = Hammer( @graph.svgEl, { swipe : false} )
      .on("drag", @throttledDragMove)
      .on("dragstart", @dragStart)
      .on("dragend", @dragEnd)

    @$el.find(".btn.dropdown-toggle").addClass("active")
    app.trigger "behavior:disable_panning"

  deactivate : ->

    @hammerContext
      .off("drag", @throttledDragMove)
      .off("dragstart", @dragStart)
      .off("dragend", @dragEnd)

    @preview.attr("d", "M 0,0 L 0,0") # move it out of the way
    @preview.classed("hidden, true")

    @$el.find(".btn.dropdown-toggle").removeClass("active")
    app.trigger "behavior:enable_panning"

  dragEnd : (event) =>

    Cluster(@cluster).finalize()
    @graph.addCluster(@cluster)
    @preview.classed("hide", true)

    app.trigger "behavior:done"


  dragStart : (event) =>

    @cluster = new DataItem(
      id : @graph.nextId()
      waypoints : []
      content : []
    )
    @preview.data(@cluster)


  dragMove : (event) =>

    position = @mouseToSVGLocalCoordinates(event)

    @cluster.get("waypoints").add(position)

    @preview
      .classed("hide", false)
      .attr("d", Cluster(@cluster).getLineSegment(@graph))

