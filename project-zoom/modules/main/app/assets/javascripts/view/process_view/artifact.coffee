### define
jquery : $
###


class Artifact

  PRIMARY_TYP : "primaryThumbnail"
  SECONDARY_TYP : "secondaryThumbnail"
  FAIL_IMAGE : "assets/images/unknown.png"

  _domElement : null
  name : null


  constructor : (@dataItem, @width, bare = false, image) ->

    @name = dataItem.get("name")

    unless image
      image = document.createElementNS("http://www.w3.org/2000/svg", "image")
      image.setAttributeNS("http://www.w3.org/1999/xlink","href", @getNearestPrimary(0))
      image.setAttribute("x","0")
      image.setAttribute("y","0")
      image.setAttribute("data-id", @dataItem.get("id"))
      image.setAttribute("class", "node")
      image.setAttribute("draggable", "false")

    unless bare
      @container = $("<div/>", {
          class: "subcontainer"
      })
      @svg = document.createElementNS("http://www.w3.org/2000/svg", "svg")
      $(@svg).append(image)
      @svg.setAttribute("class", "artifact")
      @container.append(@svg)
      @container.append("<div class=\"subcontainer-text\">#{dataItem.get("name")}</div>")
    
    @image = image

    @resize()


  resize : () =>

    width = @width()

    @image.setAttribute('width',width)
    @image.setAttribute('height',width)
    @svg?.setAttribute('width',width)
    @svg?.setAttribute('height',width)
    @container?.css('width',width) 

    width = @image.getBoundingClientRect().width

    @image.setAttributeNS('http://www.w3.org/1999/xlink','href', @getNearestPrimary(width))


  onMouseEnter : () =>

    width = @width()

    width = @image.getBoundingClientRect().width
    @image.setAttributeNS('http://www.w3.org/1999/xlink','href', @getNearestSecondary(width))


  getNearestPrimary : (width) ->

    path = @getNearest(width, @PRIMARY_TYP) || @FAIL_IMAGE


  getNearestSecondary : (width) ->

    path = @getNearest(width, @SECONDARY_TYP) || @getNearestPrimary(width)


  getResourceResolution : (resource) ->

    name = resource.get("name")
    +name.match(/(\d+)\.(png|gif)$/)[1]


  getNearest : (width, typ) ->

    resources = @dataItem.get("resources").filter( (a) -> a.get("typ") is typ )

    closestResource
    for resource in resources
      if not closestResource? or
      Math.abs(@getResourceResolution(resource) - width) <= Math.abs(@getResourceResolution(closestResource) - width)
        closestResource = resource


    if closestResource?
      "/artifacts/#{@dataItem.get("id")}/#{closestResource.get("typ")}/#{closestResource.get("name")}"


  getSvgElement : ->

    @svg

  getContainerElement : ->

    @container 


  getImage : ->

    @image


  destroy : ->

    @deactivate()


  activate : ->

    { image } = @

    $(image).on("mouseenter", @onMouseEnter)
    $(image).on("mouseleave", @resize)


  deactivate : ->

    { image } = @

    $(image).off("mouseenter", @onMouseEnter)
    $(image).off("mouseleave", @resize)


  hide : ->

    @container?.css("display","none") 


  show : ->
    
    @container?.css("display","inline-block") 
