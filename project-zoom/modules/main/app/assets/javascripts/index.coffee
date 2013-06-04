define(
  "app"
  ["./lib/application", "backbone.deepmodel", "./lib/core_ext"]
  (Application) -> window.app = new Application()
)


define [
  "backbone",
  "app",
  "bootstrap"
  ], (Backbone, app) ->

  require [
    # "testProjectsOverview", "jquery.hammer"
    "testGraph"
    "view/toasts"
    "sample"
    "model"
    "view"
  ], -> app.start( test : 123 )

