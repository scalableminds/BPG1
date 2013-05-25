### define
jquery : $
###


class Artifact

  domElement : null
  imagePaths : null


  constructor : (@artifact, @width) ->

    @imagePaths = []
    for resource in artifact.resources

      if resource.type isnt "thumbnail"
        continue

      #image = new Image().src = resource.path
      @imagePaths.push resource.path

    image = $("<img>",
        src: @imagePaths[0].src
        draggable: false
        title: artifact.name
        class: "artifact-image"
        "data-id": artifact.id)

    image.on("mouseenter", => @onMouseEnter())
    image.on("mouseleave", => @resize())

    @domElement = $("<div/>",
      title: "#{artifact.name}"
      class: "node-object artifact"
    ).append(image)
    

    @domElement.width(width())

    @resize()


  resize : () =>

    width = @width()

    return unless @domElement?
    @domElement.width(width)
    @domElement.height(width)
    $img = @domElement.find("img")

    width = @domElement[0].getBoundingClientRect().width

    $img.attr("src", @getNearest(width, "thumbnail").path)


  onMouseEnter : () =>

    width = @width()

    return unless @domElement?
    @domElement.width(width)
    @domElement.height(width)
    width = @domElement[0].getBoundingClientRect().width
    $img = @domElement.find("img")
    $img.attr("src", @getNearest(width, "secondary_thumbnail").path)


  getNearest : (width, type) ->

    elements = _.filter(@artifact.resources, (r) -> r.type is type)
    elements = _.sortBy(elements, (e) -> e.path)
    resolutions = _.map(elements, (e) -> (Number) e.path.substring(e.path.length - 5, e.path.length - 4))

    closest = 10
    base = 2 << 4
    for r in resolutions
      closest = r if not closest? or Math.abs((base << r) - width) < Math.abs((base << closest) - width)

    _.find(elements, (e) => ((Number) e.path.substring(e.path.length - 5, e.path.length - 4)) is closest)


  destroy : ->

  activate : ->

  deactivate : ->


