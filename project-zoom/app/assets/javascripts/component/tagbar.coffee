### define
jquery : $
###


class Tagbar

  TAB_PREFIX : "tab"

  domElement : null
  artifacts : null

  SAMPLE_PROJECT : {
    name:"test1"
    tags : [
      {type :"project_partner", name : "SAP"}
      {type :"date", path : "2013"}
      {type :"topic", path : "Health"}
      {type :"topic", path : "Energy"}
    ]
  }

  constructor : () ->

    @tags = []

    domElement = $('<div/>', {

    })

    slider = $("<input/>", {
      id : "defaultSlider"
      type : "range"
      min : "1"
      max : "500"
    })

    domElement.append(slider)

    project = @SAMPLE_PROJECT

    func = -> this.value
    x = _.bind(func, slider[0])


    # slider.on(
    #   "change"
    #   => artifactC.resize()
    # )

    @domElement = domElement


  setResized : (func) ->
    @onResized = func


  destroy : ->

  activate : ->

  deactivate : ->