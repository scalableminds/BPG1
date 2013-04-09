### define
lib/event_mixin : EventMixin
lib/chai : chai
###

describe "EventMixin", ->

  beforeEach ->

    dummyDispatcher = { register : (->) , unregister : (->) }
    @eventMixin = new EventMixin(dummyDispatcher)
    @spy = chai.spy()
    @self = {}


  describe "#on / #off", ->

    it "should work", ->

      @eventMixin.on(@self, "test", @spy)
      @eventMixin.trigger("test", "testArg")

      @spy.should.have.been.called.once

      @eventMixin.off(@self, "test", @spy)
      @eventMixin.trigger("test", "testArg")

      @spy.should.have.been.called.once


    it "should work without self-reference", ->

      @eventMixin.on("test", @spy)
      @eventMixin.trigger("test", "testArg")

      @spy.should.have.been.called.once

      @eventMixin.off("test", @spy)
      @eventMixin.trigger("test", "testArg")

      @spy.should.have.been.called.once


    it "should work with an array of callbacks", ->

      spy2 = chai.spy()

      @eventMixin.on(@self, "test", [ @spy, spy2 ])
      @eventMixin.trigger("test", "testArg")

      @spy.should.have.been.called.once
      spy2.should.have.been.called.once

      @eventMixin.off(@self, "test", [ @spy, spy2 ])
      @eventMixin.trigger("test", "testArg")

      @spy.should.have.been.called.once
      spy2.should.have.been.called.once


    it "should work with a map of callbacks", ->

      spy2 = chai.spy()

      @eventMixin.on(@self, 
        "test" : [ @spy, spy2 ]
        "test2" : [ @spy ]
      )
      @eventMixin.trigger("test", "testArg")
      @eventMixin.trigger("test2", "testArg")

      @spy.should.have.been.called.twice
      spy2.should.have.been.called.once

      @eventMixin.off(@self, 
        "test" : [ @spy, spy2 ]
        "test2" : [ @spy ]
      )
      @eventMixin.trigger("test", "testArg")
      @eventMixin.trigger("test2", "testArg")

      @spy.should.have.been.called.twice
      spy2.should.have.been.called.once


  describe "#times", ->

    it "should only be called 3-times", ->

      @eventMixin.times(@self, "test", @spy, 3)

      for i in [1..4]
        @eventMixin.trigger("test", "testArg")

      @spy.should.have.been.called.exactly(3)

