### define
app : app
jquery : $
view/projects_overview_view : ProjectsOverviewView
###

app.addInitializer (options, callback) ->

  view = new ProjectsOverviewView()

  callback()