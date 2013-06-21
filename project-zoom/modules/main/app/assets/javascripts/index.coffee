define(
  "app"
  ["./lib/application", "./lib/core_ext"]
  (Application) -> window.app = new Application()
)


define [
  "app",
  "bootstrap"
  ], (app) ->

  require [
    "view/toasts"
    "model"
    "view"
  ], -> app.start( test : 123 )

