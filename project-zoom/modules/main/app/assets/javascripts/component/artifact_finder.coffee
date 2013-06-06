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

    app.on "behavior:zooming", @resize

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

      artifactC = new Artifact(artifact, getSliderValue)
      @artifactComponents.push artifactC
      domElement.append(artifactC.getSvgElement())

      group = _.find(@groups, (g) => g.name is artifact.get("source"))
      group.div.append(artifactC.getSvgElement())


  setResized : (func) ->

    @onResized = func


  resize : =>

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

    artifact = new Artifact(
        @artifactsModel.find( (artifact) -> artifact.get("id") == id )
        (-> 64)
        bare
      )

    @artifactComponents.push artifact

    artifact

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

