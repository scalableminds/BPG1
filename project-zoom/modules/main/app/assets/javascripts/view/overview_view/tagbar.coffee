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
        id: "container_#{tag.name}"
      )

      $checkbox = $("<input>",
        type: "checkbox"
        id: "checkbox_#{tag.name}"
        name: tag.name
        value: tag.name
      )

      $label = $("<label>",
        id: "label_#{tag.name}"
        )
      $label.text tag.name

      $container.append $checkbox
      $container.append $label

      $taglist.append $container


  init_tag_count : (projects) ->

    # for project in projects

      # console.log project.tags


  destroy : ->

  activate : ->

  deactivate : ->





