### define
jquery : $
###


class Artifact

  domElement : null
  images : null


  constructor : (artifact) ->

    $(window).resize( (e) => @resized(e))

    images = []

    domElement = $('<div/>', {
      title: "#{artifact.name}"
      class: "artifact-wrapper"      
    })

    for resource in artifact.resources

      if resource.type isnt "thumbnail"
        continue

      image = $('<img/>', {
        title: "#{artifact.name}"
        class: "artifact-image"
        src: resource.path
      })

      images.push image


    @domElement = domElement
    @images = images


  resized : (e) ->

    width = $(window).width()
    @domElement.empty()
    if width > 800
      @domElement.append(@images[2])
    else if width > 400
      @domElement.append(@images[1])
    else
      @domElement.append(@images[0])      

  destroy : ->

  activate : ->

  deactivate : ->


