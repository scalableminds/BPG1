require.config(
  shim :
    "lib/chai-spies" : [ "lib/chai" ]
)

define [
  "lib/chai"
  "lib/chai-spies"
  "lib/mocha"
  "lib/core_ext"
], (chai, chaiSpies) ->

  # Chai
  chai.should()
  chai.use(chaiSpies)

  # Mocha
  mocha.setup("bdd")

  # Require base tests before starting
  require [
    "./test/event_mixin"
    "./test/event_dispatcher"
  ], ->

    if window.mochaPhantomJS
      mochaPhantomJS.run()
    else 
      mocha.run()


