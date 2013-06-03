### define
core_ext : CoreExt
hammer : Hammer
./behavior : Behavior
../cluster : Cluster
lib/data_item : DataItem
###

class DrawClusterBehavior extends Behavior

  constructor : ( @graph, @container, @type ) ->

    @throttledDragMove = _.throttle(@dragMove, 50)
    if $(".preview").length == 0
      @preview = @container.insert("svg:path",":first-child") #prepend for proper zOrdering
      @preview
        .attr("class", "hide preview cluster")
    else
      @preview = d3.select(".preview")


  activate : ->

    @hammerContext = Hammer( $("#process-graph")[0], { swipe : false} )
      .on("drag", @throttledDragMove)
      .on("dragstart", @dragStart)
      .on("dragend", @dragEnd)


  deactivate : ->

    @hammerContext
      .off("drag", @throttledDragMove)
      .off("dragstart", @dragStart)
      .off("dragend", @dragEnd)

    @preview.attr("d", "M 0,0 L 0,0") # move it out of the way
    @preview.classed("hidden, true")


  dragEnd : (event) =>

    Cluster(@cluster).finalize()
    @graph.addCluster(@cluster)
    @preview.classed("hide", true)

    # switch to drag tool again (reset)
    window.setTimeout( (->$(".btn-group a").first().trigger("click")), 100)


  dragStart : (event) =>

    @cluster = new DataItem(
      id : @graph.nextId()
      waypoints : []
      content : []
    )
    @preview.data(@cluster)

    @offset = $("#process-graph").offset()
    @scaleValue = $(".zoom-slider input").val()


  dragMove : (event) =>

    mouse = @mousePosition(event)

    @cluster.get("waypoints").add(mouse)

    @preview
      .classed("hide", false)
      .attr("d", Cluster(@cluster).getLineSegment())

