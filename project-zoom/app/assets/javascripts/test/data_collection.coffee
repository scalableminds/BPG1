### define
lib/data_item : DataItem
lib/request : Request
jquery : $
###

describe "DataCollection", ->

  beforeEach ->

    @dataCollection = new DataItem.Collection()


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


  describe "#extendParts", ->

    it "should merge with before part", ->

      @dataCollection.extendParts(0, 10)

      @dataCollection.parts.should.have.length(1)
      @dataCollection.parts[0].should.eql( start : 0, end : 10 )

      @dataCollection.extendParts(10, 10)

      @dataCollection.parts.should.have.length(1)
      @dataCollection.parts[0].should.eql( start : 0, end : 20 )


    it "should merge with after part", ->

      @dataCollection.extendParts(10, 10)

      @dataCollection.parts.should.have.length(1)
      @dataCollection.parts[0].should.eql( start : 10, end : 20 )

      @dataCollection.extendParts(0, 10)

      @dataCollection.parts.should.have.length(1)
      @dataCollection.parts[0].should.eql( start : 0, end : 20 )


    it "should merge in between", ->

      @dataCollection.extendParts(0, 5)
      @dataCollection.extendParts(10, 5)

      @dataCollection.parts.should.have.length(2)
      @dataCollection.parts[0].should.eql( start : 0, end : 5 )
      @dataCollection.parts[1].should.eql( start : 10, end : 15 )

      @dataCollection.extendParts(5, 5)

      @dataCollection.parts.should.have.length(1)
      @dataCollection.parts[0].should.eql( start : 0, end : 15 )


  describe "#fetch", ->

    it "should insert data", (done) ->

      sinon.stub(Request, "send").returns( 
        (new $.Deferred()).resolve( offset : 0, limit : 2, items : [ { test : "1" }, { test : "2" } ] ).promise()
      )

      @dataCollection.fetch(0, 10).then(
        =>
          Request.send.restore()

          @dataCollection.should.have.length(2)

          sinon.stub(Request, "send").returns(
            (new $.Deferred()).resolve( offset : 3, limit : 1, items : [ { test : "3" }, { test : "4" } ] ).promise()
          )
          @dataCollection.fetch(3, 10).then(
            =>
              Request.send.restore()

              @dataCollection.should.have.length(4)
              @dataCollection.parts.should.have.length(2)

              done()
          )

      )


  describe "#fetchNext", ->

    it "should load continous data", (done) ->

      sinon.stub(Request, "send").returns( 
        (new $.Deferred()).resolve( offset : 0, limit : 2, items : [ { test : "1" }, { test : "2" } ] ).promise()
      )
      @dataCollection.fetchNext().then(
        =>
          Request.send.restore()
          @dataCollection.should.have.length(2)

          sinon.stub(Request, "send").returns(
            (new $.Deferred()).resolve( offset : 2, limit : 2, items : [ { test : "3" }, { test : "4" } ] ).promise()
          )
          @dataCollection.fetchNext().then(
            =>
              Request.send.restore()
              @dataCollection.should.have.length(4)
              @dataCollection.parts.should.have.length(1)
              done()
          )

      )










