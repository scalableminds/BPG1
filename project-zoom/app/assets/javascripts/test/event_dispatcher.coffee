### define
lib/event_mixin : EventMixin
lib/event_dispatcher : EventDispatcher
lib/chai : chai
###

describe "EventDispatcher", ->

  beforeEach ->

    @dispatcher = new EventDispatcher()
    @eventMixin = new EventMixin(@dispatcher)
    @spy = chai.spy()
    @self = {}


  describe "#register", ->

    it "should create entries", ->

      @dispatcher.register = chai.spy(@dispatcher.register)
      @eventMixin.on(@self, "test", @spy)

      @dispatcher.boundObjects
        .should.have.deep.property("#{@eventMixin.__uid}[0].sender", @eventMixin)
      @dispatcher.boundObjects
        .should.have.deep.property("#{@self.__uid}[0].target", @self)

      @dispatcher.register.should.have.been.called.once



  describe "#unregister", ->

    it "should remove entries", ->

      @dispatcher.unregister = chai.spy(@dispatcher.unregister)

      @eventMixin.on(@self, "test", @spy)
      @eventMixin.off(@self, "test", @spy)

      @dispatcher.boundObjects
        .should.not.have.property(@eventMixin.__uid)
      @dispatcher.boundObjects
        .should.not.have.property(@self.__uid)

      @dispatcher.unregister.should.have.been.called.once


  describe "#unregisterAll", ->

    it "should work", ->

      @dispatcher.unregister = chai.spy(@dispatcher.unregister)
      @eventMixin.on(@self, "test", @spy)
      @eventMixin.trigger("test", "testArg")

      @spy.should.have.been.called.once

      @dispatcher.unregisterAll(@self)
      @eventMixin.trigger("test", "testArg")

      @spy.should.have.been.called.once
      @dispatcher.unregister.should.have.been.called.once


