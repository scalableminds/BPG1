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

      @dataItem.lazyAttributes.test = url : "test"

      @dataItem.get("test/test2", this, (value) =>

        Request.send.restore()
        value.should.equal("test2")
        @dataItem.attributes.should.have.property("test")
        @dataItem.lazyAttributes.should.not.have.property("test")
        done()
      )



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

