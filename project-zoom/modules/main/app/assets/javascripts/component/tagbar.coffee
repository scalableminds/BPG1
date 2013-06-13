### define
jquery : $
app : app
###


class Tagbar

  domElement : null


  constructor : (@tagCollection, @$el) ->

    domElement = $("<div/>")
    @domElement = domElement

    @initTags()
    @populateTagForm()


  setResized : (func) ->

    @onResized = func


  initTags : ->

    @tags = []

    @tagCollection.forEach( (tag) =>

      t =
        id:           tag.get("id")
        name:         tag.get("name")
        color:        tag.get("color")

      @tags.push t
    )


  populateTagForm : ->

    $taglist = @$el.find("#taglist")

    for tag in @tags

      $container = $("<div>",
        class: "tagbarItem"
      )

      $checkbox = $("<input>",
        type: "checkbox"
        name: tag.name
        value: tag.name
      )

      $label = $("<label>")
      $label.text tag.name

      $container.append $checkbox
      $container.append $label

      $taglist.append $container


  destroy : ->

  activate : ->

  deactivate : ->





