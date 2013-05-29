### define
jquery : $
app: app
underscore : _
./artifact : Artifact
###


class ArtifactFinder

  GROUP_NAMES : ["Box", "Dropbox", "dummy"]
  TAB_PREFIX : "tab"

  domElement : null
  groups : null
  onResize : null
  slider : null

  SAMPLE_ARTIFACTS : [
    {
      name:"test1"
      id : 12345
      source : "Box"
      resources : [
        {typ :"primaryThumbnail", name: "32.png", path : "assets/images/thumbnails/primary_thumbnail/32.png"}
        {typ :"primaryThumbnail", name: "64.png", path : "assets/images/thumbnails/primary_thumbnail/64.png"}
        {typ :"primaryThumbnail", name: "128.png", path : "assets/images/thumbnails/primary_thumbnail/128.png"}
        {typ :"primaryThumbnail", name: "256.png", path : "assets/images/thumbnails/primary_thumbnail/256.png"}
        {typ :"secondaryThumbnail", name: "256.png", path : "assets/images/thumbnails/secondary_thumbnail/256.gif"}
        {typ :"secondaryThumbnail", name: "512.png", path : "assets/images/thumbnails/secondary_thumbnail/512.gif"} 
        {typ :"original",  name: "32.png", path : "assets/images/thumbnails/fail.png"}
      ]
    }
    {
      name:"test2"
      id : 12346
      source : "Box"
      resources : [
        {typ :"primaryThumbnail", name: "32.png", path : "assets/images/thumbnails/primary_thumbnail/32.png"}
        {typ :"primaryThumbnail", name: "64.png", path : "assets/images/thumbnails/primary_thumbnail/64.png"}
        {typ :"primaryThumbnail", name: "128.png", path : "assets/images/thumbnails/primary_thumbnail/128.png"}
        {typ :"primaryThumbnail", name: "256.png", path : "assets/images/thumbnails/primary_thumbnail/256.png"}
        {typ :"secondaryThumbnail", name: "256.png", path : "assets/images/thumbnails/secondary_thumbnail/256.gif"}
        {typ :"secondaryThumbnail", name: "512.png", path : "assets/images/thumbnails/secondary_thumbnail/512.gif"} 
        {typ :"original",  name: "32.png", path : "assets/images/thumbnails/fail.png"}
      ]
    }      
    {
      name:"test3"
      id : 12347
      source : "Dropbox"
      resources : [
        {typ :"primaryThumbnail", name: "32.png", path : "assets/images/thumbnails/primary_thumbnail/32.png"}
        {typ :"primaryThumbnail", name: "64.png", path : "assets/images/thumbnails/primary_thumbnail/64.png"}
        {typ :"primaryThumbnail", name: "128.png", path : "assets/images/thumbnails/primary_thumbnail/128.png"}
        {typ :"primaryThumbnail", name: "256.png", path : "assets/images/thumbnails/primary_thumbnail/256.png"}
        {typ :"secondaryThumbnail", name: "256.png", path : "assets/images/thumbnails/secondary_thumbnail/256.gif"}
        {typ :"secondaryThumbnail", name: "512.png", path : "assets/images/thumbnails/secondary_thumbnail/512.gif"} 
        {typ :"original",  name: "32.png", path : "assets/images/thumbnails/fail.png"}
      ]
    }  
  ]

  constructor : (@artifactsModel) ->

    @groups = []
    @artifactComponents = []

    domElement = $('<div/>', {
      class : "artifact-container"
    })

    @createGroups(domElement, @GROUP_NAMES)

    @domElement = domElement
    @initSlider(domElement)

    app.model.project.get("artifacts", @, (a) => @addArtifacts(a.items))

    @resizeHandler = =>
      @domElement.height($(window).height() - @domElement.offset().top - 30)


  initSlider : (domElement) -> 

    slider = $("<input/>", {
      class : "artifact-slider"
      type : "range"
      min : "32"
      max : "400"
      value: "40"
    })
    @onResize = => @resize()

    func = -> this.value
    @getSliderValue = _.bind(func, slider[0])

    domElement.prepend(slider)
    @slider = slider


  addArtifacts : (artifacts) ->

    { group, getSliderValue, domElement } = @
     
    for artifact in artifacts

      artifactC = new Artifact(artifact.attributes, getSliderValue)    
      @artifactComponents.push artifactC
      domElement.append(artifactC.getSvgElement())     

      group = _.find(@groups, (g) => g.name is artifact.attributes.source)
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
    @slider.on(
      "change"
      @onResize
    )
    @resizeHandler()

    for artifact in @artifactComponents
      artifact.activate()


  deactivate : ->

    $(window).off("resize", @resizeHandler)
    @slider.off(
      "change"
      @onResize
    )

    for artifact in @artifactComponents
      artifact.deactivate()


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

