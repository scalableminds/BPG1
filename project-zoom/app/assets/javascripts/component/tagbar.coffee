### define
jquery : $
###


class Tagbar

  domElement : null
  taglist = [
    {type :"project_partner", name : "SAP"},
    {type :"date", name : "2013"},
    {type :"branch", name : "Health"},
    {type :"branch", name : "Energy"},
  ]


  constructor : () ->
    @tags = taglist

    domElement = $('<div/>', {})

    project = @SAMPLE_PROJECT
    @domElement = domElement


  setResized : (func) ->
    @onResized = func


  populateTagForm : ->

    branchTaglist = $("#branchtags")
    dateTaglist = $("#datetags")
    partnerTaglist = $("#partnertags")

    for tag of @tags
      tagName = @tags[tag].name
      tagType = @tags[tag].type

      checkbox = document.createElement("input")
      checkbox.type = "checkbox"
      checkbox.name = tagName
      checkbox.value = tagName

      label = document.createElement("label")
      label.innerHTML = tagName

      listToAppend = null
      switch tagType
        when "date" then listToAppend = dateTaglist
        when "project_partner" then listToAppend = partnerTaglist
        when "branch" then listToAppend = branchTaglist
        else console.log "tag with strange type"

      listToAppend.append checkbox
      listToAppend.append label


  destroy : ->

  activate : ->

  deactivate : ->