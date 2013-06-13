### define
jquery : $
text!templates/process_view_toolbar.html : ToolbarTemplate
###

class GUI

  constructor : (@$el, @artifactFinder) ->

    @appendArtifactFinder()
    @appendSVG()
    @appendToolbar()

    @windowResize()


  activate : ->

    @initToggleHandler()
    @initToolbarHandler()


  deactivate : ->

    @$el.find(".toolbar .btn").off("click")
    @$el.find("a.toggles").off("click")


  windowResize : ->

    @svg.attr("height", $(window).height() - @$el.find(".graph").offset().top - 30)


  appendToolbar : ->

    @$el.find("#main").prepend(ToolbarTemplate)


  appendSVG : ->

    @svg = d3.select(@$el[0]).select(".graph")
      .append("svg")
      .attr(
        id: "process-graph"
        width: @$el.find(".graph").width()
        "pointer-events": "all"
      )


  initToolbarHandler : ->

    @$el.find(".toolbar .btn").on "click", (event) =>
      @$el.find(".toolbar .btn").removeClass('active')

      $this = $(event.target)
      unless $this.hasClass('active')
        $this.addClass('active')

      event.preventDefault()


  initToggleHandler : ->

    @$el.find("a.toggles").click =>
      @$el.find("a.toggles i").toggleClass "icon-chevron-left icon-chevron-right"
      @$el.find("#artifact-finder").toggle()
      @$el.find("#main").toggleClass "span12 span8"


  appendArtifactFinder : ->

    @$el.find("#artifact-finder").append( @artifactFinder.domElement )
    #make first tab activate
    @$el.find("a[data-toggle=tab]").first().tab("show")
