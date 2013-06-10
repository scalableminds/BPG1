### define
jquery : $
app: app
underscore : _
./artifact : Artifact
###


class ArtifactFinder

  GROUP_NAMES : ["folder", "date"]

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
    @windowResize()


  windowResize : ->

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

    paths = _.uniq(_.map(artifacts, (a) -> a.get("path"))).sort()

    group = @groups[0]

    
    group.div.append("<div class=\"accordion\">")
    folder = []
    for path in paths         
      group.div.append(
        @accordionDocTemplate { path, bodyId : "collapseBody#{path.replace(/\//g,"_")}" }
      )
    group.div.append("</div>")
    

    for artifact in artifacts

      path = artifact.get("path")
      parent = $("#collapseBody#{path.replace(/\//g,"_")}").find(".accordion-inner")

      artifactC = new Artifact(artifact, getSliderValue)
      @artifactComponents.push artifactC
      parent.append(artifactC.getContainerElement())


  setResized : (func) ->

    @onResized = func


  resize : =>

    for artifact in @artifactComponents
      artifact.resize()


  destroy : ->

    @deactivate()


  activate : ->

    @slider.on("change", @onResize)
    app.on(this, "behavior:zooming", @resize)
    @windowResize()

    for artifact in @artifactComponents
      artifact.activate()


  deactivate : ->

    @slider.off("change", @onResize)
    app.off(this, "behavior:zooming", @resize)

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


  accordionDocTemplate : _.template """
    <div class="accordion-group">
      <div class="accordion-heading">
        <a class="accordion-toggle"
          data-toggle="collapse"
          data-target="#<%= bodyId %>"
          href="#">
          <%= path %>
        </a>
      </div>
      <div id="<%= bodyId %>" class="accordion-body collapse in">
        <div class="accordion-inner">
        </div>
      </div>
    </div>
  """
