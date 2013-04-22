require.config(
  shim :
    "lib/sinon-chai" : [ "lib/sinon" ]
    "lib/sinon" : 
      exports : "sinon"

)

define [
  "lib/chai"
  "lib/sinon-chai"
  "lib/mocha"
  "lib/core_ext"
], (chai, chaiSinon) ->

  # Chai
  chai.should()
  chai.use(chaiSinon)

  # Mocha
  mocha.setup("bdd")

  # Require base tests before starting
  require [
    "./test/event_mixin"
    "./test/event_dispatcher"
    "./test/data_item"
    "./test/data_collection"
  ], ->

    if window.mochaPhantomJS
      mochaPhantomJS.run()
    else 
      mocha.run()


