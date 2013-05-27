### define
jquery : $
###

class GUI

  constructor : ->

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

  initSVG : ->

    width = $(".graph").width()

    @svg = d3.select(".graph")
      .append("svg")
      .attr("width", width)
      .attr("height", @height)
      .attr("pointer-events", "all")


  initToolbar : ->

    $('.btn-group .btn').on "click", (event) ->
      $('.btn-group .btn').removeClass('active')

      $this = $(@)
      unless $this.hasClass('active')
        $this.addClass('active')

      event.preventDefault()


  initSideBar : ->

    $(".side-bar").css("height", @height)


