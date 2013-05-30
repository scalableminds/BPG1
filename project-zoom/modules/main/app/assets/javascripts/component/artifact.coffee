### define
jquery : $
###


class Artifact

  PRIMARY_TYP : "primaryThumbnail"
  SECONDARY_TYP : "secondaryThumbnail"
  FAIL_IMAGE : "assets/images/unknown.png"

  _domElement : null


  constructor : (@artifact, @width, bare = false) ->

    image = document.createElementNS("http://www.w3.org/2000/svg", "image")
    image.setAttributeNS('http://www.w3.org/1999/xlink','href', @getNearestPrimary(0))
    image.setAttribute('x','0')
    image.setAttribute('y','0')
    image.setAttribute('data-id', @artifact.id)

    unless bare
      @svg = document.createElementNS("http://www.w3.org/2000/svg", "svg")
      $(@svg).append(image)
      @svg.setAttribute("class", "artifact")

    @image = image

    @resize()


  resize : () =>

    width = @width()

    @image.setAttribute('width',width)
    @image.setAttribute('height',width)
    @svg?.setAttribute('width',width)
    @svg?.setAttribute('height',width)

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


  getNearest : (width, typ) ->

    elements = _.filter(@artifact.resources.items, (r) -> r.attributes.typ is typ)
    resolutions = _.map(elements, (e) -> (Number) e.attributes.name.substring(0, e.attributes.name.lastIndexOf(".")))

    closest = null
    for r in resolutions
      closest = r if not closest? or Math.abs(r - width) < Math.abs(closest - width)

    e =_.find(elements, (e) => (((Number) e.attributes.name.substring(0, e.attributes.name.lastIndexOf("."))) is closest))
    path = null
    if e?
      path = "/artifacts/#{@artifact.id}/#{e.attributes.typ}/#{e.attributes.name}"
    return path


  getSvgElement : ->

    @svg


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


