### define
core_ext : CoreExt
hammer : Hammer
###

class DrawClusterBehavior

  constructor : ( @graph ) ->

    @throttledDragMove = _.throttle(@dragMove, 50)


  activate : ->

    @hammerContext = Hammer( $("svg")[0] )
      .on("dragstart", @dragStart)
      .on("drag", @throttledDragMove)
      .on("dragend", @dragEnd)


  deactivate : ->

    @hammerContext
      .off("drag", @throttledDragMove)
      .off("dragend", @dragEnd)
      .off("dragstart", @dragStart)


  dragEnd : (event) =>

    @graph.addCluster(@waypoints)


  dragStart : (event) =>

    @waypoints = []

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

    @waypoints.push(tmp)


