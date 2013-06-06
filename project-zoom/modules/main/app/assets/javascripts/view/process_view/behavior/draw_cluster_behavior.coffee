### define
core_ext : CoreExt
hammer : Hammer
./behavior : Behavior
../cluster : Cluster
lib/data_item : DataItem
app : app
###

class DrawClusterBehavior extends Behavior

  constructor : ( @graph, @container, @type ) ->

    @throttledDragMove = _.throttle(@dragMove, 50)
    if @graph.$svgEl.find(".preview").length == 0
      @preview = @graph.graphContainer.insert("svg:path",":first-child") #prepend for proper zOrdering
      @preview
        .attr("class", "hide preview cluster")
    else
      @preview = @graph.d3Element.select(".preview")


  activate : ->

    @hammerContext = Hammer( @graph.svgEl, { swipe : false} )
      .on("drag", @throttledDragMove)
      .on("dragstart", @dragStart)
      .on("dragend", @dragEnd)

    app.trigger "behavior:disable_panning"

  deactivate : ->

    @hammerContext
      .off("drag", @throttledDragMove)
      .off("dragstart", @dragStart)
      .off("dragend", @dragEnd)

    @preview.attr("d", "M 0,0 L 0,0") # move it out of the way
    @preview.classed("hidden, true")

    app.trigger "behavior:enable_panning"

  dragEnd : (event) =>

    Cluster(@cluster).finalize()
    @graph.addCluster(@cluster)
    @preview.classed("hide", true)

    # switch to drag tool again (reset)
    window.setTimeout( ( -> $(".btn-group a").first().trigger("click")), 100)


  dragStart : (event) =>

    @cluster = new DataItem(
      id : @graph.nextId()
      waypoints : []
      content : []
    )
    @preview.data(@cluster)

    @offset = @graph.$svgEl.offset()
    @scaleValue = app.view.zoom.level


  dragMove : (event) =>

    mouse = @mousePosition(event)

    @cluster.get("waypoints").add(mouse)

    @preview
      .classed("hide", false)
      .attr("d", Cluster(@cluster).getLineSegment())

