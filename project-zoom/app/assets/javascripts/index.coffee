
define(
  "app"
  ["./lib/application", "backbone.deepmodel", "./lib/core_ext"]
  (Application) -> new Application()
)


define [
  "backbone",
  "app",
  "jquery.hammer"
  "bootstrap"
  ], (Backbone, app) ->

  require [
    "testGraph", "jquery.hammer"
    "sample"
    "model"
  ], -> app.start( test : 123 )



