### define
jquery : $
underscore : _
./artifact : Artifact
###


class ArtifactFinder

  GROUP_NAMES : ["Dropbox", "Incom", "FileShare"]
  TAB_PREFIX : "tab"

  domElement : null
  groups : null
  artifacts : null

  SAMPLE_ARTIFACT : { 
    name:"test1"
    source : "Dropbox"
    resources : [
      {type :"thumbnail", path : "assets/images/thumbnails/0.png"}
      {type :"thumbnail", path : "assets/images/thumbnails/1.png"}
      {type :"thumbnail", path : "assets/images/thumbnails/2.png"}
      {type :"thumbnail", path : "assets/images/thumbnails/3.png"}
      {type :"original", path : "assets/images/thumbnails/fail.png"}
    ]
  }

  constructor : () ->

    @groups = []
    @artifactComponents = []

    domElement = $('<div/>', {

    })

    slider = $("<input/>", {
      id : "defaultSlider"
      type : "range"
      min : "1"
      max : "500"
      value: "40"
    })

    domElement.append(slider)

    artifact = @SAMPLE_ARTIFACT

    @createGroups(domElement, @GROUP_NAMES)

    func = -> this.value
    x = _.bind(func, slider[0])


    artifactC = new Artifact(
      artifact
      x
    )
    @artifactComponents.push artifactC

    slider.on(
      "change"
      => artifactC.resize()
    )    

    domElement.append(artifactC.domElement)

    group = _.find(@groups, (g) => g.name is artifact.source)
    group.div.append(artifactC.domElement)

    @domElement = domElement


  setResized : (func) ->
    @onResized = func



  destroy : ->

  activate : ->

  deactivate : ->


  pluginDocTemplate : _.template """
    <div class="tabbable tabs-left">
      <ul class="nav nav-tabs">
        <% groupNames.forEach(function (group) { %>
          <li>
            <a data-toggle="tab"
              href="#tab<%= group %>">
              <%= group %>
            </a>
          </li>
        <% }) %>
      </ul>
      <div class="tab-content">
        <% groupNames.forEach(function (group) { %>
          <div class="tab-pane" id="tab<%= group %>">
            <p>
              <%= group %>test
            </p>
          </div>
        <% }) %>
      </div>
    </div>
  """  


  createGroups : (parent, groupNames) ->

    { groups } = @

    tabs = @pluginDocTemplate { groupNames }
    parent.append(tabs)  
    for name in groupNames
      groups.push { name: name, div: parent.find("#tab#{name}")}    