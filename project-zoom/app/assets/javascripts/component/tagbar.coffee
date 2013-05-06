### define
jquery : $
###


class Tagbar

  domElement : null
  taglist = [
    {type :"project_partner", name : "SAP"},
    {type :"date", name : "2013"},
    {type :"topic", name : "Health"},
    {type :"topic", name : "Energy"},
  ]


  constructor : () ->
    @tags = taglist

    domElement = $('<div/>', {})

    project = @SAMPLE_PROJECT
    @domElement = domElement


  setResized : (func) ->
    @onResized = func


  populateTagForm : ->

    checkBoxList = $("#chklist")

    for tag of @tags
      pair = @tags[tag].name
      checkbox = document.createElement("input")

      checkbox.type = "checkbox"
      checkbox.name = pair
      checkbox.value = pair

      checkBoxList.append checkbox

      label = document.createElement("label")
      label.htmlFor = pair
      label.appendChild document.createTextNode(pair)

      checkBoxList.append label
      checkBoxList.append document.createElement("br")


  destroy : ->

  activate : ->

  deactivate : ->