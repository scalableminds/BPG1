### define
lib/data_item : DataItem
lib/json_patch_accumulator : JsonPatchAccumulator
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


    it "should not not remove elements not in the collection", ->

      dataItem1 = new DataItem(test : "test1")
      dataItem2 = new DataItem(test : "test2")

      @dataCollection.add(dataItem1)
      @dataCollection.length.should.be.equal(1)

      @dataCollection.remove(dataItem2)
      @dataCollection.length.should.be.equal(1)


    it "should work with paths", ->

      dataItem1 = new DataItem(test : "test1")
      @dataCollection.add(dataItem1)

      @dataCollection.get("0/test").should.equal("test1")

      @dataCollection.set("0/test", "test2")
      @dataCollection.get("0/test").should.equal("test2")

    it "should update", ->

      @dataCollection.add("test2")

      @dataCollection.update(0, (value) ->
        value.should.equal("test2")
        "test3"
      )

      @dataCollection.at(0).should.equal("test3")


    it "shouldn't allow holes in collection", ->

      @dataCollection.set(0, "test0")
      @dataCollection.set(2, "test1")

      @dataCollection.should.have.length(2)
      @dataCollection.at(0).should.be.equal("test0")
      @dataCollection.at(1).should.be.equal("test1")


  describe "changes", ->

    it "should trigger add events", (done) ->

      dataItem = new DataItem(test : "test")
      @dataCollection.add("test")

      async.parallel([

        (callback) =>
          @dataCollection.on(this, "patch:add", (index, value, collection) =>
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
          @dataCollection.on(this, "patch:remove", (index, value, collection) =>
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

      @dataCollection.one(this, "patch:remove", (index, value) =>
        value.should.equal(dataItem)
        dataItem.__callbacks["patch:*"].should.have.length(changeCallbackCount - 1)
        done()
      )
      changeCallbackCount = dataItem.__callbacks["patch:*"].length
      @dataCollection.remove(dataItem)



    it "should not trigger events with silent option", (done) ->

      @dataCollection.on(this, "patch:*", @spy)

      @dataCollection.set(0, "test1", silent : true)
      @dataCollection.set(0, "test1")

      setTimeout(
        =>
          @spy.should.have.been.called.once
          done()
        10
      )


  describe "json patch", ->

    it "should record member add", ->

      patchAcc = JsonPatchAccumulator.attach(@dataCollection)
      @dataCollection.add("test")
      @dataCollection.add("test1")
      @dataCollection.set(1, "test2")

      jsonPatch = patchAcc.flush()

      jsonPatch.should.deep.equal(
        [
          { op : "add", path : "/0", value : "test" }
          { op : "add", path : "/1", value : "test2" }
        ]
      )



  describe "at/get", ->

    it "should retrieve elements", ->

      @dataCollection.add("test1", "test2")
      @dataCollection.at(1).should.equal("test2")


    it "should retrieve elements synchronously", ->

      @dataCollection.add("test1")

      @dataCollection.get("0").should.equal("test1")


  describe "fetching", ->

    it "should insert data", (done) ->

      sinon.stub(Request, "send").returns(
        (new $.Deferred()).resolve( offset : 0, limit : 2, content : [ { test : "1" }, { test : "2" } ] ).promise()
      )

      @dataCollection.fetch(0, 10).then(
        =>
          Request.send.restore()

          @dataCollection.at(0).get("test").should.equal("1")
          @dataCollection.at(1).get("test").should.equal("2")
          @dataCollection.should.have.length(2)

          sinon.stub(Request, "send").returns(
            (new $.Deferred()).resolve( offset : 2, limit : 1, content : [ { test : "3" }, { test : "4" } ] ).promise()
          )
          @dataCollection.fetch(3, 10).then(
            =>
              Request.send.restore()

              @dataCollection.at(2).get("test").should.equal("3")
              @dataCollection.should.have.length(3)

              done()
          )

      )

    it "should load continous data", (done) ->

      sinon.stub(Request, "send").returns(
        (new $.Deferred()).resolve( offset : 0, limit : 2, content : [ { test : "1" }, { test : "2" } ] ).promise()
      )
      @dataCollection.fetchNext().then(
        =>
          Request.send.restore()
          @dataCollection.at(0).get("test").should.equal("1")
          @dataCollection.at(1).get("test").should.equal("2")
          @dataCollection.should.have.length(2)

          sinon.stub(Request, "send").returns(
            (new $.Deferred()).resolve( offset : 2, limit : 2, content : [ { test : "3" }, { test : "4" } ] ).promise()
          )
          @dataCollection.fetchNext().then(
            =>
              Request.send.restore()
              @dataCollection.at(2).get("test").should.equal("3")
              @dataCollection.at(3).get("test").should.equal("4")
              @dataCollection.should.have.length(4)
              done()
          )

      )


  describe "export", ->

    it "should export a plain object", ->

      @dataCollection.add(new DataItem(test : "test2"))

      @dataCollection.toObject().should.deep.equal([{test : "test2"}])





