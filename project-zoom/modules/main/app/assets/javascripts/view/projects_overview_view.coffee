### define
lib/event_mixin : EventMixin
underscore : _
jquery : $
d3 : d3
app : app
./overview/projectGraph : ProjectGraph
./overview/gui : GUI
../component/tagbar : Tagbar
text!templates/overview_view.html : OverviewTemplate
./overview/behavior/pan_zoom_behavior : PanZoomBehavior
###

class ProjectsOverviewView

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

    @graph.drawProjects()


  deactivate : ->

    @$el.off("dragstart")

    @$el.find(".btn-group a").off("click")
    @$el.find(".zoom-slider")
      .off("change")
      .off("click")

    @$el.find(".tagbarItem input").off("click")

    @gui.deactivate()
    @panning.deactivate()
    @tagbar.deactivate()


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
        tags:         [project.get("year")]

      @projects.push p
    )

    @graph = new ProjectGraph(@el, @projects)








