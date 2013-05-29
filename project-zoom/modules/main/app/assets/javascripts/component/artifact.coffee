### define
jquery : $
###


class Artifact

  _domElement : null
  imagePaths : null


  constructor : (@artifact, @width, bare = false) ->

    @imagePaths = []
    for resource in artifact.resources

      if resource.type isnt "thumbnail"
        continue

      @imagePaths.push resource.path

    image = document.createElementNS("http://www.w3.org/2000/svg", "image")
    image.setAttributeNS('http://www.w3.org/1999/xlink','href', @imagePaths[0])
    image.setAttribute('x','0')
    image.setAttribute('y','0')
    image.setAttribute('data-id', @artifact.id)

    $(image).on("mouseenter", => @onMouseEnter())
    $(image).on("mouseleave", => @resize())


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

    @image.setAttributeNS('http://www.w3.org/1999/xlink','href', @getNearest(width, "thumbnail").path)


  onMouseEnter : () =>

    width = @width()

    width = @image.getBoundingClientRect().width
    @image.setAttributeNS('http://www.w3.org/1999/xlink','href', @getNearest(width, "secondary_thumbnail").path)


  getNearest : (width, type) ->

    elements = _.filter(@artifact.resources, (r) -> r.type is type)
    elements = _.sortBy(elements, (e) -> e.path)
    resolutions = _.map(elements, (e) -> (Number) e.path.substring(e.path.length - 5, e.path.length - 4))

    closest = null
    base = 2 << 4
    for r in resolutions
      closest = r if not closest? or Math.abs((base << r) - width) < Math.abs((base << closest) - width)

    _.find(elements, (e) => ((Number) e.path.substring(e.path.length - 5, e.path.length - 4)) is closest)


  getSvgElement : ->

    @svg


  getImage : ->

    @image


  destroy : ->

  activate : ->

  deactivate : ->


