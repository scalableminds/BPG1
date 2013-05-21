### define
lib/data_item : DataItem
lib/sinon : sinon
lib/chai : chai
lib/request : Request
async : async
###

describe "DataItem", ->

  beforeEach ->

    @dataItem = new DataItem()
    DataItem.lazyCache = {}
    @spy = sinon.spy()


  describe "set/get", ->

    it "should set a property", (done) ->

      @dataItem.set("test", "testValue")

      @dataItem.get("test", @dataItem, 
        (value) => 
          value.should.be.equal("testValue")
          done()
      )



    it "should get/set nested data structures", (done) ->

      @dataItem.set(
        test :
          test1 : "test"
      )

      @dataItem.attributes.test.should.be.instanceof(DataItem)
      @dataItem.attributes.test.attributes.test1.should.equal("test")

      @dataItem.set("test/test2", "test2")

      @dataItem.attributes.test.attributes.test2.should.equal("test2")

      async.parallel [

        (callback) =>
          @dataItem.get("test/test1", this, (value) ->
            value.should.equal("test")
            callback()
          )

        (callback) =>
          @dataItem.get("test/test2", this, (value) ->
            value.should.equal("test2")
            callback()
          )

      ], done



    it "should get/set nested collections", (done) ->

      @dataItem.set(
        test : [
          { test1 : "test" }
          { test2 : "test" }
        ]
      )

      async.parallel([

        (callback) =>
          @dataItem.get("test", this, (value) =>

            value.should.be.instanceof(DataItem.Collection)
            value.should.have.length(2)

            value.at(0).should.be.instanceof(DataItem)

            value.at(0).get("test1", this, (value) ->
              value.should.equal("test")
              callback()
            )
          )

        (callback) =>
          @dataItem.get("test/1/test2", this, (value) ->
            value.should.equal("test")
            callback()
          )

      ], done)


    it "should get attributes synchronously", ->

      @dataItem.set(
        test :
          test1 : "test"
      )

      @dataItem.get("test/test1").should.equal("test")


    it "should keep the 'this' context", (done) ->

      @dataItem.set(
        test :
          test1 : "test"
      )

      that = this
      @dataItem.get("test/test1", that, (value) ->
        this.should.equal(that)
        done()
      )


  describe "changes", ->


    it "should trigger local change events", (done) ->

      async.parallel([
        
        (callback) =>
          @dataItem.one(this, "change:test", (value, obj) => 
            value.should.equal("test2")
            obj.should.equal(@dataItem)
            callback()
          )

        (callback) =>
          @dataItem.one(this, "change", (changeSet, obj) => 
            changeSet.should.deep.equal({ test : "test2" })
            obj.should.equal(@dataItem)
            callback()
          )

      ], done)

      @dataItem.set("test", "test2")



    it "should propagate change events", (done) ->

      @dataItem.set(
        test :
          test1 : "test"
      )

      @dataItem.one(this, "change", (changeSet, obj) =>
        changeSet.test.test1.should.equal("test2")
        obj.should.equal(@dataItem)
        done()
      )

      @dataItem.set("test/test1", "test2")



    it "should trigger unset events and remove change tracking", (done) ->

      @dataItem.set(
        test :
          test1 : "test"
      )

      @dataItem.get("test", this, (subDataItem) =>

        @dataItem.one(this, "change:test", (value, obj) =>
          chai.expect(value).to.be.undefined
          obj.should.equal(@dataItem)
          @dataItem.attributes.should.not.have.property("test")

          subDataItem.__callbacks.change.should.have.length(changeCallbackCount - 1)

          done()
        )

        changeCallbackCount = subDataItem.__callbacks.change.length
        @dataItem.unset("test")
      )


    it "should accumulate changes", (done) ->

      @dataItem.set(
        test :
          test1 : "test"
      )

      @dataItem.set("test2", "test2")

      @dataItem.unset("test2")

      changeSet = @dataItem.changeAcc.flush()
      changeSet.should.deep.equal(
        test :
          test1 : "test"
        test2 : undefined
      )

      changeSet.__timestamp.should.be.closeTo(Date.now(), 10)
      done()



  describe "lazy attributes", ->

    it "should fetch lazy attributes", (done) ->

      sinon.stub(Request, "send").returns( 
        (new $.Deferred()).resolve( { test2 : "test2" } ).promise()
      )

      @dataItem.lazyAttributes.test = "/test"

      @dataItem.get("test/test2", this, (value) =>

        Request.send.restore()
        value.should.equal("test2")
        @dataItem.attributes.should.have.property("test")
        @dataItem.lazyAttributes.should.not.have.property("test")
        done()
      )


    it "should detect and load lazy attributes", (done) ->

      sinon.stub(Request, "send").returns( 
        (new $.Deferred()).resolve( { test2 : "test2" } ).promise()
      )

      @dataItem.set(
        _test : "id123"
      )

      @dataItem.get("test", this, (value) =>

        Request.send.should.have.been.calledWithMatch( url : "/tests/id123" )
        Request.send.restore()
        value.should.be.instanceof(DataItem)
        value.get("test2", this, (value) ->
          value.should.equal("test2")
          done()
        )
      )


    it "should detect and load lazy collections", (done) ->

      do ->
        i = 0
        sinon.stub(Request, "send", ->
          (new $.Deferred()).resolve( { test2 : "test#{i++}" } ).promise()
        )

      @dataItem.set(
        _tests : ("id#{i}" for i in [0..2])
      )

      @dataItem.get("tests", this, (value) =>

        Request.send.should.have.been.calledThrice
        for i in [0..2]
          Request.send.getCall(i).should.have.been.calledWithMatch( url : "/tests/id#{i}" )
        Request.send.restore()

        value.should.be.instanceof(DataItem.Collection)
        async.parallel(
          [0..2].map (i) =>
            (callback) => 
              value.at(i).get("test2", this, (value) ->
                value.should.equal("test#{i}")
                callback()
              )
          done
        )

      )


    it "should make sure two equal lazy attributes are equal objects", (done) ->

      sinon.stub(Request, "send", ->
        (new $.Deferred()).resolve( { test2 : "test2" } ).promise()
      )

      @dataItem.set(
        _test : "id321"
      )

      dataItem2 = new DataItem(
        _test : "id321"
      )

      async.parallel([

        (callback) =>
          @dataItem.get("test", this, (value) ->
            callback(null, value)
          )

        (callback) =>
          dataItem2.get("test", this, (value) ->
            callback(null, value)
          )

      ], (err, [a, b]) ->
        Request.send.should.have.been.calledOnce
        Request.send.restore()
        a.should.equal(b)
        done()
      )


  describe "prepare DataItem", ->

    it "should be prepare items twice", ->

      value = { test : "test2" }

      dataItem = DataItem.prepareValue(value)
      dataItem.should.be.instanceof(DataItem)
      dataItem.should.equal(DataItem.prepareValue(dataItem))


    it "should prepare colletions twice", ->

      value = [ { test : "test2" }, { test : "test2" } ]

      dataItem = DataItem.prepareValue(value)
      dataItem.should.be.instanceof(DataItem.Collection)
      dataItem.should.equal(DataItem.prepareValue(dataItem))


  describe "export", ->

    it "should export a plain object", ->

      @dataItem.set(
        test :
          test2 : "test2"
      )

      @dataItem.toObject().should.deep.equal(
        test :
          test2 : "test2"
      )

