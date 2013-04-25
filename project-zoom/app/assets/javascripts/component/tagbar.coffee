### define
jquery : $
###


class Tagbar

  TAB_PREFIX : "tab"

  domElement : null
  artifacts : null


  constructor : () ->
    @tags = []
    @selectedTags = []

    domElement = $('<div/>', {})

    project = @SAMPLE_PROJECT
    @domElement = domElement


  setResized : (func) ->
    @onResized = func


  arrangeProjectGraph : () ->


  observeCheckboxes : () ->

    arr = $("input[type=checkbox]:checked").map( ->
      @value
    ).get()

    console.log arr


    # $("#my_checkbox").click ->
    #   if $(this).is(":checked")
    #     $("input[name=\"totalCost\"]").val 10
    #   else
    #     calculate()


  destroy : ->

  activate : ->

  deactivate : ->