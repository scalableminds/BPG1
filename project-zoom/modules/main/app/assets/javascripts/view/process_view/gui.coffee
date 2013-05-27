### define
jquery : $
###

class GUI

  constructor : (@artifactFinder) ->

    margin = 20
    @height = $(window).height() - $(".graph").offset().top - margin

    @appendArtifactFinder()
    @appendSVG()


  activate : ->

    @initSideBar()
    @initToggleHandler()
    @initToolbarHandler()


  deactivate : ->

    $(".btn-group .btn").off("click")
    $("a.toggles").off("click")


  appendSVG : ->

    @svg = d3.select(".graph")
      .append("svg")
      .attr("width", $(".graph").width())
      .attr("pointer-events", "all")

    $(window).resize(
      => @svg.attr("height", $(window).height() - $(".graph").offset().top - 30)
    ).resize()


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
      $("#artifact-finder").animate
        width: "toggle"
      , 100
      $("#main").toggleClass "span12 span8"


  appendArtifactFinder : ->

    $("#artifact-finder").append( @artifactFinder.domElement )
    #make first tab activate
    $("a[data-toggle=tab]").first().tab("show")
