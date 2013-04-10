### define
lib/data_collection : DataCollection
lib/chai : chai
###

describe "DataCollection", ->

  beforeEach ->

    @dataCollection = new DataCollection()


  describe "#length", ->

    it "should have a length", ->

      @dataCollection.add("test")
      @dataCollection.length.should.be.equal(1)


  describe "#add/#remove", ->

    it "should add and remove many elements", ->

      @dataCollection.add("test1", "test2")
      @dataCollection.length.should.be.equal(2)

      @dataCollection.remove("test1", "test2")
      @dataCollection.length.should.be.equal(0)


  describe "#at", ->

    it "should retrieve elements", ->

      @dataCollection.add("test1", "test2")
      @dataCollection.at(1).should.equal("test2")






