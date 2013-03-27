### define
jquery : $
###


class Artifact

  domElement : null
  images : null


  constructor : (artifact, @width) ->

    images = []

    @domElement = $('<div/>', {
      title: "#{artifact.name}"
    })

    @domElement.width(width())

    for resource in artifact.resources

      if resource.type isnt "thumbnail"
        continue

      image = $('<img/>', {
        title: "#{artifact.name}"
        class: "artifact-image"
        src: resource.path
      })

      images.push image
    
    @domElement.append(images[0])
    
    @images = images


  resize : () ->

    width = @width() 

    return unless @domElement?
    @domElement.width(width)

    width = @domElement[0].getBoundingClientRect().width

    @domElement.empty()
    if width > 200
      @domElement.append(@images[2])
    else if width > 100
      @domElement.append(@images[1])
    else
      @domElement.append(@images[0])      


  destroy : ->

  activate : ->

  deactivate : ->


