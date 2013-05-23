### define
jquery : $
###


class Artifact

  domElement : null
  images : null


  constructor : (artifact, @width) ->

    @images = []
    for resource in artifact.resources

      if resource.type isnt "thumbnail"
        continue

      #image = new Image().src = resource.path
      @images.push resource.path

    @domElement = $("<div/>",
      title: "#{artifact.name}"
      class: "node-object"
    ).append(
      $("<img>",
        src: @images[0].src
        draggable: false
        title: artifact.name
        class: "artifact-image"
        "data-id": artifact.id
      )
    )

    @domElement.width(width())

    @resize()


  resize : () =>

    width = @width()

    return unless @domElement?
    @domElement.width(width)
    $img = @domElement.find("img")

    width = @domElement[0].getBoundingClientRect().width

    if width > 192
      $img.attr("src", @images[3])
    else if width > 96
      $img.attr("src", @images[2])
    else if width > 48
      $img.attr("src", @images[1])
    else
      $img.attr("src", @images[0])


  destroy : ->

  activate : ->

  deactivate : ->


