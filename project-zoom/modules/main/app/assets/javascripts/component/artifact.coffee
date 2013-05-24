### define
jquery : $
###


class Artifact

  domElement : null
  imagePaths : null


  constructor : (artifact, @width) ->

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

    if width > 192
      $img.attr("src", @imagePaths[3])
    else if width > 96
      $img.attr("src", @imagePaths[2])
    else if width > 48
      $img.attr("src", @imagePaths[1])
    else
      $img.attr("src", @imagePaths[0])


  destroy : ->

  activate : ->

  deactivate : ->


