### define
lib/data_item : DataItem
lib/sinon : sinon
async : async
###

describe "DataItem", ->

  beforeEach ->

    @dataItem = new DataItem()
    @spy = sinon.spy()


  describe "#set/#get", ->

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



    it "should trigger local change events", (done) ->

      async.parallel([
        
        (callback) =>
          @dataItem.on(this, "change:test", (value, obj) => 
            value.should.equal("test2")
            obj.should.equal(@dataItem)
            callback()
          )

        (callback) =>
          @dataItem.on(this, "change", (set, obj) => 
            set.should.deep.equal({ test : "test2" })
            obj.should.equal(@dataItem)
            callback()
          )

      ], done)

      @dataItem.set("test", "test2")




