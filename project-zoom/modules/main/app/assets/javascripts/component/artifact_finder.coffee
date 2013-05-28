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

  SAMPLE_ARTIFACTS : [
    {
      name:"test1"
      id : 12345
      source : "Dropbox"
      resources : [
        {type :"thumbnail", id : 123, path : "assets/images/thumbnails/thumbnail/0.png"}
        {type :"thumbnail", id : 456, path : "assets/images/thumbnails/thumbnail/1.png"}
        {type :"thumbnail", id : 789, path : "assets/images/thumbnails/thumbnail/2.png"}
        {type :"thumbnail", id : 112, path : "assets/images/thumbnails/thumbnail/3.png"}
        {type :"secondary_thumbnail", id : 1, path : "assets/images/thumbnails/secondary_thumbnail/2.gif"}
        {type :"secondary_thumbnail", id : 2, path : "assets/images/thumbnails/secondary_thumbnail/3.gif"} 
        {type :"original",  id : 345, path : "assets/images/thumbnails/fail.png"}
      ]
    }
    {
      name:"test2"
      id : 12346
      source : "Dropbox"
      resources : [
        {type :"thumbnail", id : 123, path : "assets/images/thumbnails/thumbnail/0.png"}
        {type :"thumbnail", id : 456, path : "assets/images/thumbnails/thumbnail/1.png"}
        {type :"thumbnail", id : 789, path : "assets/images/thumbnails/thumbnail/2.png"}
        {type :"thumbnail", id : 112, path : "assets/images/thumbnails/thumbnail/3.png"}
        {type :"secondary_thumbnail", id : 1, path : "assets/images/thumbnails/secondary_thumbnail/2.gif"}
        {type :"secondary_thumbnail", id : 2, path : "assets/images/thumbnails/secondary_thumbnail/3.gif"} 
        {type :"original",  id : 345, path : "assets/images/thumbnails/fail.png"}
      ]
    }      
    {
      name:"test3"
      id : 12347
      source : "Incom"
      resources : [
        {type :"thumbnail", id : 123, path : "assets/images/thumbnails/thumbnail/0.png"}
        {type :"thumbnail", id : 456, path : "assets/images/thumbnails/thumbnail/1.png"}
        {type :"thumbnail", id : 789, path : "assets/images/thumbnails/thumbnail/2.png"}
        {type :"thumbnail", id : 112, path : "assets/images/thumbnails/thumbnail/3.png"}
        {type :"secondary_thumbnail", id : 1, path : "assets/images/thumbnails/secondary_thumbnail/2.gif"}
        {type :"secondary_thumbnail", id : 2, path : "assets/images/thumbnails/secondary_thumbnail/3.gif"}        
        {type :"original",  id : 345, path : "assets/images/thumbnails/fail.png"}
      ]
    }  
  ]

  constructor : () ->

    @groups = []
    @artifactComponents = []

    domElement = $('<div/>', {
      class : "artifact-container"
    })

    @createGroups(domElement, @GROUP_NAMES)

    @domElement = domElement
    @initSlider(domElement)
    @addArtifacts(@SAMPLE_ARTIFACTS)

    @resizeHandler = =>
      @domElement.height($(window).height() - @domElement.offset().top - 30)


  initSlider : (domElement) -> 

    slider = $("<input/>", {
      class : "finder-slider"
      type : "range"
      min : "1"
      max : "500"
      value: "40"
    })
    slider.on(
      "change"
      => @resize()
    )

    func = -> this.value
    @getSliderValue = _.bind(func, slider[0])

    domElement.prepend(slider)


  addArtifacts : (artifacts) ->

    { group, getSliderValue, domElement } = @
     
    for artifact in artifacts

      artifactC = new Artifact(artifact, getSliderValue)    
      @artifactComponents.push artifactC
      domElement.append(artifactC.getSvgElement())     

      group = _.find(@groups, (g) => g.name is artifact.source)
      group.div.append(artifactC.getSvgElement())


  setResized : (func) ->

    @onResized = func


  resize : ->

    for artifact in @artifactComponents
      artifact.resize()


  destroy : ->

    @deactivate()


  activate : ->

    $(window).on("resize", @resizeHandler)
    @resizeHandler()


  deactivate : ->

    $(window).off("resize", @resizeHandler)



  getArtifact : (id, bare = false) =>

    for artifact in @SAMPLE_ARTIFACTS
      if artifact.id = id
        return new Artifact( artifact, (-> 64), bare)

  pluginDocTemplate : _.template """
    <div class="tabbable tabs-top">
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

