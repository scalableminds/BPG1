### define
./behavior : Behavior
jquery.mousewheel : Mousewheel
app : app
view/wheel : Wheel
###

class ZoomBehavior extends Behavior

  constructor : (@$el, @graph) ->

    @graphContainer = @graph.graphContainer[0][0] #get the DOM Element not the D3 object
    @svgRoot = $el.find("#process-graph")[0]

    #@zoom(.2)
    @wheel = new Wheel(@$el.find(".graph"))

    super(@graph)


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

    transformationMatrix = @graphContainer.getCTM()
    transformationMatrix.a = transformationMatrix.d = scaleValue

    # @setCTM(transformationMatrix)

    app.trigger "behavior:zooming"
