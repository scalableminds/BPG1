### define
lib/event_mixin : EventMixin
lib/event_dispatcher : EventDispatcher
lib/sinon : sinon
async : async
###

describe "EventDispatcher", ->

  beforeEach ->

    @dispatcher = new EventDispatcher()
    @eventMixin = new EventMixin(@dispatcher)
    @spy = sinon.spy()
    @self = {}


  describe "#register", ->

    it "should create entries", ->

      @dispatcher.register = sinon.spy(@dispatcher.register)
      @eventMixin.on(@self, "test", @spy)

      @dispatcher.boundObjects
        .should.have.deep.property("#{@eventMixin.__uid}[0].sender", @eventMixin)
      @dispatcher.boundObjects
        .should.have.deep.property("#{@self.__uid}[0].target", @self)

      @dispatcher.register.should.have.been.called.once


    it "should return a self-nooping callback", ->

      callback = @dispatcher.register(@eventMixin, @self, null, @spy)
      
      callback("test")
      @spy.should.have.been.called.once

      @dispatcher.unregister(@eventMixin, @self, null, @spy)

      callback("test")
      @spy.should.have.been.called.once


    it "should return a one-shot callback", ->

      callback = @dispatcher.register(@eventMixin, @self, null, @spy)

      callback.oneShot("test")
      @spy.should.have.been.called.once
      
      callback("test")
      @spy.should.have.been.called.once


    it "should execute in sender's context", (done) ->

      that = this
      async.parallel [
        
        (callback) =>
          @dispatcher.register(@eventMixin, @self, null, ->
            this.should.equal(that.self)
            callback()
          )()

        (callback) =>
          @dispatcher.register(@eventMixin, @self, null, ->
            this.should.equal(that.self)
            callback()
          ).oneShot()

      ], done


    it "should work without a sender", ->

      callback = @dispatcher.register(@self, @spy)

      callback("test")
      @spy.should.have.been.called.once
      
      @dispatcher.unregister(@self, @spy)

      callback("test")
      @spy.should.have.been.called.once



  describe "#unregister", ->

    it "should remove entries", ->

      @dispatcher.unregister = sinon.spy(@dispatcher.unregister)

      @eventMixin.on(@self, "test", @spy)
      @eventMixin.off(@self, "test", @spy)

      @dispatcher.boundObjects
        .should.not.have.property(@eventMixin.__uid)
      @dispatcher.boundObjects
        .should.not.have.property(@self.__uid)

      @dispatcher.unregister.should.have.been.called.once


    it "should only remove the first entry", ->

      @dispatcher.unregister = sinon.spy(@dispatcher.unregister)

      @eventMixin.on(@self, "test", @spy)
      @eventMixin.on(@self, "test", @spy)

      @dispatcher.boundObjects
        .should.have.deep.property("#{@eventMixin.__uid}.length", 2)
      @dispatcher.boundObjects
        .should.have.deep.property("#{@self.__uid}.length", 2)

      @eventMixin.off(@self, "test", @spy)

      @dispatcher.boundObjects
        .should.have.deep.property("#{@eventMixin.__uid}.length", 1)
      @dispatcher.boundObjects
        .should.have.deep.property("#{@self.__uid}.length", 1)


    it "should work with already deleted entries", ->

      @dispatcher.unregister = sinon.spy(@dispatcher.unregister)

      @eventMixin.on(@self, "test", @spy)
      @eventMixin.off(@self, "test", @spy)
      @eventMixin.off(@self, "test", @spy)


  describe "#unregisterAll", ->

    it "should work", ->

      @dispatcher.unregister = sinon.spy(@dispatcher.unregister)
      @eventMixin.on(@self, "test", @spy)
      @eventMixin.trigger("test", "testArg")

      @spy.should.have.been.called.once

      @dispatcher.unregisterAll(@self)
      @eventMixin.trigger("test", "testArg")

      @spy.should.have.been.called.once
      @dispatcher.unregister.should.have.been.called.once


