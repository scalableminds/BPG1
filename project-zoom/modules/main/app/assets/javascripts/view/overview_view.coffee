### define
lib/event_mixin : EventMixin
underscore : _
jquery : $
d3 : d3
app : app
./overview_view/projectGraph : ProjectGraph
./overview_view/gui : GUI
./overview_view/tagbar : Tagbar
./overview_view/behavior/pan_zoom_behavior : PanZoomBehavior
text!templates/overview_view.html : OverviewTemplate
###

class OverviewView

  IMAGE_FOLDER = "/assets/images/letter_images/"

  constructor : (@projectsCollection) ->

    EventMixin.extend(this)
    @$el = $(OverviewTemplate)
    @el = @$el[0]

    @initTagbar()
    @gui = new GUI(@tagbar, @$el)
    @initGraph()
    @tagbar.init_tag_count(@projects)

    @panning = new PanZoomBehavior(@$el, @graph)


  initTagbar : ->

    @tagbar = new Tagbar(app.model.tags, @$el)
    @$el.find("#tagbar").append( @tagbar.domElement )


  activate : ->

    @gui.activate()
    @tagbar.activate()
    @panning.activate()

    @$el.find(".tagbarItem input").on "click", (event) => @graph.updateVennDiagram(event.currentTarget)

    # drag artifact into graph
    @$el.on( "dragstart", "#artifact-finder .artifact-image", (e) -> e.preventDefault() )

    # @hh = Hammer(@$el.find(".graph svg")[0]).on("tap", "image", (event) -> 
    #   app.model.setProject(d3.select(event.target).datum())
    # )
    

    @graph.drawProjects()


  deactivate : ->

    @$el.off("dragstart")

    @$el.find(".btn-group a").off("click")
    @$el.find(".zoom-slider")
      .off("change")
      .off("click")

    @$el.find(".tagbarItem input").off("click")

    # @hh.off("tap")

    @gui.deactivate()
    @panning.deactivate()
    @tagbar.deactivate()


  hittest : (x,y) ->

    target = document.elementFromPoint(x, y)
    d3.select(target).datum()?.original


  initGraph : ->

    @projects = []

    @projectsCollection.forEach( (project) =>

      p =
        id:           project.get("id")
        name:         project.get("name")
        season:       project.get("season")
        year:         project.get("year")
        length:       project.get("length")
        participants: project.get("participants")
        image:        IMAGE_FOLDER.concat "#{project.get("name")[0].toLowerCase()}.png"
        width:        "100px"
        height:       "100px"
        tags:         @clean_tags project.get("tags").toObject() # [project.get("year")]
        original:     project

      @projects.push p
    )

    @graph = new ProjectGraph(@el, @projects)


  clean_tags : (tag_list) ->

    result_tags = []

    for tag in tag_list
      result_tags.push tag.name

    result_tags



