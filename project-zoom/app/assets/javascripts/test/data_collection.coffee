### define
lib/data_item : DataItem
lib/request : Request
lib/chai : chai
jquery : $
async : async
###

describe "DataItem.Collection", ->

  beforeEach ->

    @dataCollection = new DataItem.Collection()


  describe "length", ->

    it "should have a length", ->

      @dataCollection.add(new DataItem(test : "test"))
      @dataCollection.length.should.be.equal(1)


  describe "add/remove", ->

    it "should add and remove many elements", ->

      dataItem1 = new DataItem(test : "test1")
      dataItem2 = new DataItem(test : "test2")

      @dataCollection.add(dataItem1, dataItem2)
      @dataCollection.length.should.be.equal(2)

      @dataCollection.remove(dataItem1, dataItem2)
      @dataCollection.length.should.be.equal(0)


  describe "changes", ->

    it "should trigger add events", (done) ->

      dataItem = new DataItem(test : "test")
      @dataCollection.add("test")

      async.parallel([

        (callback) =>
          @dataCollection.on(this, "add", (value, index, collection) =>
            value.should.equal(dataItem)
            index.should.equal(1)
            collection.should.equal(@dataCollection)
            callback()
          )

        (callback) =>
          @dataCollection.on(this, "change:1", (value, collection) =>
            value.should.equal(dataItem)
            collection.should.equal(@dataCollection)
            callback()
          )

        (callback) =>
          @dataCollection.on(this, "change", (changeSet, collection) =>
            changeSet[1].should.equal(dataItem)
            collection.should.equal(@dataCollection)
            callback()
          )

      ], done)

      @dataCollection.add(dataItem)


    it "should trigger remove events", (done) ->

      dataItem = new DataItem(test : "test")
      @dataCollection.add("test")
      @dataCollection.add(dataItem)

      async.parallel([

        (callback) =>
          @dataCollection.on(this, "remove", (value, index, collection) =>
            value.should.equal(dataItem)
            index.should.equal(1)
            collection.should.equal(@dataCollection)
            callback()
          )

        (callback) =>
          @dataCollection.on(this, "change:1", (value, collection) =>
            chai.expect(value).to.be.undefined
            collection.should.equal(@dataCollection)
            callback()
          )

        (callback) =>
          @dataCollection.on(this, "change", (changeSet, collection) =>
            chai.expect(changeSet[1]).to.be.undefined
            collection.should.equal(@dataCollection)
            callback()
          )

      ], done)

      @dataCollection.remove(dataItem)


    it "should propagate change events", (done) ->

      dataItem = new DataItem(test : "test1")
      @dataCollection.add(dataItem)

      @dataCollection.on(this, "change", (changeSet, obj) =>
        changeSet[0].test.should.equal("test2")
        obj.should.equal(@dataCollection)
        done()
      )
      dataItem.set("test", "test2")


    it "should remove change tracking on remove", (done) ->

      dataItem = new DataItem(test : "test")
      @dataCollection.add(dataItem)

      @dataCollection.one(this, "remove", (value) =>
        value.should.equal(dataItem)
        dataItem.__callbacks.change.should.have.length(changeCallbackCount - 1)
        done()
      )
      changeCallbackCount = dataItem.__callbacks.change.length
      @dataCollection.remove(dataItem)


  describe "at", ->

    it "should retrieve elements", ->

      @dataCollection.add("test1", "test2")
      @dataCollection.at(1).should.equal("test2")


  describe "parts management", ->

    it "should merge with before part", ->

      @dataCollection.addParts(0, 10)

      @dataCollection.parts.should.have.length(1)
      @dataCollection.parts[0].should.eql( start : 0, end : 10 )

      @dataCollection.addParts(10, 10)

      @dataCollection.parts.should.have.length(1)
      @dataCollection.parts[0].should.eql( start : 0, end : 20 )


    it "should merge with after part", ->

      @dataCollection.addParts(10, 10)

      @dataCollection.parts.should.have.length(1)
      @dataCollection.parts[0].should.eql( start : 10, end : 20 )

      @dataCollection.addParts(0, 10)

      @dataCollection.parts.should.have.length(1)
      @dataCollection.parts[0].should.eql( start : 0, end : 20 )


    it "should merge in between", ->

      @dataCollection.addParts(0, 5)
      @dataCollection.addParts(10, 5)

      @dataCollection.parts.should.have.length(2)
      @dataCollection.parts[0].should.eql( start : 0, end : 5 )
      @dataCollection.parts[1].should.eql( start : 10, end : 15 )

      @dataCollection.addParts(5, 5)

      @dataCollection.parts.should.have.length(1)
      @dataCollection.parts[0].should.eql( start : 0, end : 15 )


  describe "fetching", ->

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


  describe "export", ->

    it "should export a plain object", ->

      @dataCollection.add(new DataItem(test : "test2"))

      @dataCollection.toObject().should.deep.equal([{test : "test2"}])











