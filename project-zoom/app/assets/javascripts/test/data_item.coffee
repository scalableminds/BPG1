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

    it "should get/set a nested data structure", (done) ->

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


