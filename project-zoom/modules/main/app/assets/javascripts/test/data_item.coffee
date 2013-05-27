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


    it "should update", ->

      @dataItem.set(test : "test2")

      @dataItem.update("test", (value) ->
        value.should.equal("test2")
        "test3"
      )

      @dataItem.get("test").should.equal("test3")




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

          subDataItem.__callbacks["patch:*"].should.have.length(changeCallbackCount - 1)

          done()
        )

        changeCallbackCount = subDataItem.__callbacks["patch:*"].length
        @dataItem.unset("test")
      )


    it "should accumulate changes", (done) ->

      @dataItem.set(
        test :
          test1 : "test"
          test2 : [1, 2, 3]
      )

      @dataItem.set("test3", "test3")

      @dataItem.unset("test3")

      @dataItem.get("test/test2").remove(2)

      changeSet = @dataItem.changeAcc.flush()
      changeSet.should.deep.equal(
        test :
          test1 : "test"
          test2 : [1, undefined, 3]
        test3 : undefined
      )

      changeSet.__timestamp.should.be.closeTo(Date.now(), 10)
      done()


  describe "json patch", ->

    it "should record member add/set", ->

      @dataItem.set(
        test : "test"
      )

      @dataItem.patchAcc.flush()

      @dataItem.set("test1", "test1")
      @dataItem.unset("test")

      jsonPatch = @dataItem.patchAcc.flush()

      jsonPatch.should.deep.equal(
        [
          { op : "add", path : "/test1", value : "test1" }
          { op : "remove", path : "/test" }
        ]
      )


    it "should propagate patches", ->

      @dataItem.set(
        test : [
          test2 : "test2"
        ]
      )

      @dataItem.patchAcc.flush()

      @dataItem.get("test/0").set("test2", "test3")

      jsonPatch = @dataItem.patchAcc.flush()
      jsonPatch.should.deep.equal(
        [
          { op : "replace", path : "/test/0/test2", value : "test3" }
        ]
      )

    describe "compact", ->

      it "should remove added then removed patches", ->

        @dataItem.set("test", "test2")

        @dataItem.patchAcc.peek().should.deep.equal([
          op : "add", path : "/test", value : "test2"
        ])

        @dataItem.unset("test")

        jsonPatch = @dataItem.patchAcc.compact()
        jsonPatch.should.deep.equal([])


      it "should merge redundant (replace after add) patches", ->

        @dataItem.set("test", "test2")
        @dataItem.set("test", "test3")

        jsonPatch = @dataItem.patchAcc.compact()
        jsonPatch.should.deep.equal([
          op : "add", path : "/test", value : "test3"
        ])


      it "should merge nested patches", ->

        @dataItem.set(
          test :
            test2 : "test3"
            test4 : []
        )

        @dataItem.patchAcc.peek().should.deep.equal([
          op : "add"
          path : "/test"
          value : 
            test2 : "test3"
            test4 : []
        ])

        @dataItem.set("test/test2", "test4")

        @dataItem.patchAcc.compact().should.deep.equal([
          op : "add"
          path : "/test"
          value : 
            test2 : "test4"
            test4 : []
        ])

        @dataItem.set("test/test3", "test5")
        @dataItem.get("test/test4").add("test6")

        @dataItem.patchAcc.compact().should.deep.equal([
          op : "add"
          path : "/test"
          value : 
            test2 : "test4"
            test3 : "test5"
            test4 : ["test6"]
        ])

        @dataItem.set("test/test4/0", "test7")

        @dataItem.patchAcc.compact().should.deep.equal([
          op : "add"
          path : "/test"
          value : 
            test2 : "test4"
            test3 : "test5"
            test4 : ["test7"]
        ])


      it "should merge array addings", ->

        @dataItem.set("test", [])
        @dataItem.patchAcc.flush()

        @dataItem.get("test").add("test1")
        @dataItem.get("test").add("test2")
        @dataItem.set("test/1", "test3")

        @dataItem.patchAcc.compact().should.deep.equal([
          { op : "add", path : "/test/0", value : "test1" }
          { op : "add", path : "/test/1", value : "test3" }
        ])


      it "should merge weird array operations", ->

        @dataItem.set("test", [])
        @dataItem.get("test").add("test1")
        @dataItem.get("test").add("test2")
        @dataItem.set("test/1", "test3")
        @dataItem.get("test").add("test4")

        @dataItem.patchAcc.compact().should.deep.equal([
          op : "add"
          path : "/test"
          value : [
            "test1"
            "test3"
            "test4"
          ]
        ])



      it "should compact for simple items", ->

        @dataItem.set(
          test : [
            test2 : "test2"
          ]
        )

        @dataItem.patchAcc.flush()

        @dataItem.get("test/0").set("test2", "test3")
        @dataItem.get("test/0").set("test2", "test4")

        @dataItem.get("test/0").set("test3", "test4")
        @dataItem.get("test/0").unset("test3")

        jsonPatch = @dataItem.patchAcc.compact()
        jsonPatch.should.deep.equal(
          [
            { op : "replace", path : "/test/0/test2", value : "test4" }
          ]
        )


      it "should merge nested items", ->

        @dataItem.set(
          test : [
            test2 :
              test3 : "test3"
          ]
        )

        @dataItem.set("test/0/test2/test3" : "test4")

        jsonPatch = @dataItem.patchAcc.compact()
        jsonPatch.should.deep.equal(
          [
            { 
              op : "add"
              path : "/test"
              value : [
                test2 : {
                  test3 : "test4"
                }
              ] 
            }
          ]
        )







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

