### define
jquery : $

###

class GUI

  constructor : (@$el, @artifactFinder) ->

    @appendArtifactFinder()
    @appendSVG()

    @windowResize()


  activate : ->

    @initToggleHandler()


  deactivate : ->

    @$el.find("a.toggles").off("click")


  windowResize : ->

    @svg.attr("height", $(window).height() - @$el.find(".graph").offset().top - 30)


  appendSVG : ->

    @svg = d3.select(@$el[0]).select(".graph")
      .append("svg")
      .attr(
        id: "process-graph"
        width: @$el.find(".graph").width()
        "pointer-events": "all"
      )
      

  initToggleHandler : ->

    @$el.find("a.toggles").click =>
      @$el.find("a.toggles i").toggleClass "icon-chevron-left icon-chevron-right"
      @$el.find("#artifact-finder").toggle()
      @$el.find("#main").toggleClass "span12 span8"


  appendArtifactFinder : ->

    @$el.find("#artifact-finder").append( @artifactFinder.domElement )
    #make first tab activate
    @$el.find("a[data-toggle=tab]").first().tab("show")
