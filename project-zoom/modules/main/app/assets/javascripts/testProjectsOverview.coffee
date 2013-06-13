### define
app : app
jquery : $
view/projects_overview_view : ProjectsOverviewView
###

app.on "start", ->

  view = new ProjectsOverviewView(app.model.projects)
  $(".content").append(view.el)
  view.activate()