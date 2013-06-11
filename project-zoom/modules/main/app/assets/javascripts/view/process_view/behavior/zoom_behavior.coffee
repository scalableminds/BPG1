### define
./behavior : Behavior
jquery.mousewheel : Mousewheel
app : app
view/wheel : Wheel
###

class ZoomBehavior extends Behavior

  constructor : (@$el, @graph) ->

    @zoom(.2)
    @wheel = new Wheel(@$el.find(".graph"))


  activate : ->

    app.view.process.on(this, "zoom", @zoom)

    @wheel.activate()
    @wheel.on("delta", app.view.process.changeZoom)

    @zoom()



  deactivate : ->

    app.view.process.off(this, "zoom", @zoom)
    @wheel.deactivate()
    @wheel.off("delta", app.view.process.changeZoom)



  zoom : (scaleValue = app.view.process.zoom) =>

    console.log scaleValue

    graphContainer = @graph.graphContainer

    transformation = d3.transform(graphContainer.attr("transform"))
    transformation.scale = [scaleValue, scaleValue]

    graphContainer.attr("transform", transformation.toString())

    app.trigger "behavior:zooming"
