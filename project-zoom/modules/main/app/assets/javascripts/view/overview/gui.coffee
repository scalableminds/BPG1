### define
jquery : $
###

class GUI

  constructor : (@tagbar, @$el) ->

    @appendTagbar()
    @appendSVG()

    @resizeHandler = =>
      @svg.attr("height", $(window).height() - @$el.find(".graph").offset().top - 30)


  activate : ->

    @initSideBar()
    @initToggleHandler()

    $(window).on("resize", @resizeHandler)
    @resizeHandler()


  deactivate : ->

    @$el.find(".btn-group .btn").off("click")
    @$el.find("a.toggles").off("click")
    $(window).off("resize", @resizeHandler)


  appendSVG : ->

    @svg = d3.select(@$el[0]).select(".graph")
      .append("svg")
      .attr("width", @$el.find(".graph").width())
      .attr("pointer-events", "all")

    $(window).resize(
      => @svg.attr("height", $(window).height() - @$el.find(".graph").offset().top - 30)
    ).resize()


  initSideBar : ->

    @$el.find(".side-bar").css("height", @height)


  initToggleHandler : ->

    @$el.find("a.toggles").click =>
      @$el.find("a.toggles i").toggleClass "icon-chevron-left icon-chevron-right"
      @$el.find("#tagbar").toggle()
      @$el.find("#main").toggleClass "span12 span8"


  appendTagbar : ->

    @$el.find("#tagbar").append( @tagbar.domElement )







