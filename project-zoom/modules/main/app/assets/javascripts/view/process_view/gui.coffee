### define
jquery : $
###

class GUI

  constructor : ->

    margin = 20
    @height = $(window).height() - $(".graph").offset().top - margin

    @initToolbar()
    @initSVG()
    @initSideBar()


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


