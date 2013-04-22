### define
lib/event_mixin : EventMixin
lib/sinon : sinon
###

describe "EventMixin", ->

  beforeEach ->

    @dummyDispatcher = { register : (->) , unregister : (->) }
    @eventMixin = EventMixin.extend({}, @dummyDispatcher)
    @spy = sinon.spy()
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

      spy2 = sinon.spy()

      @eventMixin.on(@self, "test", [ @spy, spy2 ])
      @eventMixin.trigger("test", "testArg")

      @spy.should.have.been.called.once
      spy2.should.have.been.called.once

      @eventMixin.off(@self, "test", [ @spy, spy2 ])
      @eventMixin.trigger("test", "testArg")

      @spy.should.have.been.called.once
      spy2.should.have.been.called.once


    it "should work with a map of callbacks", ->

      spy2 = sinon.spy()

      @eventMixin.on(@self, 
        "test" : [ @spy, spy2 ]
        "test2" : @spy
      )
      @eventMixin.trigger("test", "testArg")
      @eventMixin.trigger("test2", "testArg")

      @spy.should.have.been.calledTwice
      spy2.should.have.been.calledOnce

      @eventMixin.off(@self, 
        "test" : [ @spy, spy2 ]
        "test2" : @spy
      )
      @eventMixin.trigger("test", "testArg")
      @eventMixin.trigger("test2", "testArg")

      @spy.should.have.been.calledTwice
      spy2.should.have.been.calledOnce


    it "should only remove first callback", ->

      @eventMixin.on(@self, "test", @spy)
      @eventMixin.on(@self, "test", @spy)
      @eventMixin.trigger("test", "testArg")

      @spy.should.have.been.calledTwice

      @eventMixin.off(@self, "test", @spy)
      @eventMixin.trigger("test", "testArg")

      @spy.should.have.been.calledThrice


  describe "#times", ->

    it "should only be called 3-times", ->

      @eventMixin.times(@self, "test", @spy, 3)

      for i in [1..4]
        @eventMixin.trigger("test", "testArg")

      @spy.should.have.been.calledThrice


  describe "#hasCallbacks", ->

    it "should have callbacks", ->

      @eventMixin.on(@self, "test", @spy)
      @eventMixin.hasCallbacks("test").should.be.true



  describe "#isolatedExtend", ->

    it "should work with scrambled function names", ->

      @eventMixin = EventMixin.isolatedExtend({}, @dummyDispatcher)
      @eventMixin.addEventListener = @eventMixin.on
      delete @eventMixin.on
      delete @eventMixin.dispatcher

      @eventMixin.addEventListener(@self, test : @spy )
      @eventMixin.trigger("test", "testArg")
      @eventMixin.off(@self, "test", @spy)

      @spy.should.have.been.called.once
