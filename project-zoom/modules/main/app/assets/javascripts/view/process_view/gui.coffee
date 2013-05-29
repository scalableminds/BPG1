### define
jquery : $
###

class GUI

  constructor : (@artifactFinder) ->

    @appendArtifactFinder()
    @appendSVG()

    @resizeHandler = =>
      @svg.attr("height", $(window).height() - $(".graph").offset().top - 30)


  activate : ->

    @initSideBar()
    @initToggleHandler()
    @initToolbarHandler()

    $(window).on("resize", @resizeHandler)
    @resizeHandler()


  deactivate : ->

    $(".btn-group .btn").off("click")
    $("a.toggles").off("click")
    $(window).off("resize", @resizeHandler)


  appendSVG : ->

    @svg = d3.select(".graph")
      .append("svg")
      .attr(
        id: "process-graph"
        width: $(".graph").width()
        "pointer-events": "all"
      )


  initToolbarHandler : ->

    $(".btn-group .btn").on "click", (event) ->
      $(".btn-group .btn").removeClass('active')

      $this = $(@)
      unless $this.hasClass('active')
        $this.addClass('active')

      event.preventDefault()


  initSideBar : ->

    $(".side-bar").css("height", @height)


  initToggleHandler : ->

    $("a.toggles").click ->
      $("a.toggles i").toggleClass "icon-chevron-left icon-chevron-right"
      $("#artifact-finder").toggle()
      $("#main").toggleClass "span12 span8"


  appendArtifactFinder : ->

    $("#artifact-finder").append( @artifactFinder.domElement )
    #make first tab activate
    $("a[data-toggle=tab]").first().tab("show")
