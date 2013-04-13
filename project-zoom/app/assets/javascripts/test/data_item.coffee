### define
lib/data_item : DataItem
lib/chai : chai
###

describe "DataItem", ->

  beforeEach ->

    @dataItem = new DataItem()
    @spy = chai.spy()


  describe "#set", ->

    it "should set a property", (done) ->

      @dataItem.set("test", "testValue")

      @dataItem.get("test", @dataItem, 
        (value) => 
          value.should.be.equal("testValue")
          done()
      )


