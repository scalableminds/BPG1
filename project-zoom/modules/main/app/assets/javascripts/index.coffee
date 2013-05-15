define(
  "app"
  ["./lib/application", "backbone.deepmodel", "./lib/core_ext"]
  (Application) -> new Application()
)


define [
  "backbone",
  "app",
  "bootstrap"
  ], (Backbone, app) ->

  require [
    # "testProjectsOverview", "jquery.hammer"
    "testGraph"
    "sample"
    "model"
  ], -> app.start( test : 123 )

