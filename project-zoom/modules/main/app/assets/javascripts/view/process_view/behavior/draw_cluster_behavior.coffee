### define
core_ext : CoreExt
hammer : Hammer
./behavior : Behavior
../cluster : Cluster
###

class DrawClusterBehavior extends Behavior

  constructor : ( @graph, @container, @type ) ->

    @throttledDragMove = _.throttle(@dragMove, 50)
    if $(".preview").length == 0
      @preview = @container.insert("svg:path",":first-child") #prepend for proper zOrdering
      @preview
        .attr("class", "hidden preview cluster")
    else
      @preview = d3.select(".preview")


  activate : ->

    @hammerContext = Hammer( $("svg")[0], { swipe : false} )
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

    @cluster.finalize()
    @graph.addCluster(@cluster)
    @preview.classed("hidden, true")

    # switch to drag tool again (reset)
    window.setTimeout( (->$(".btn-group a").first().trigger("click")), 100)


  dragStart : (event) =>

    @cluster = new Cluster()
    @preview.data(@cluster)

    @offset = $("svg").offset()
    @scaleValue = $(".zoomSlider input").val()


  dragMove : (event) =>

    x = event.gesture.touches[0].pageX - @offset.left
    y = event.gesture.touches[0].pageY - @offset.top

    x /= @scaleValue
    y /= @scaleValue

    tmp =
      x : x
      y : y

    @cluster.waypoints.push(tmp)

    @preview
      .classed("hidden", false)
      .attr("d", @cluster.getLineSegment())

