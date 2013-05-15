### define
lib/data_item : DataItem
lib/chai : chai
###

describe "Integration", ->

  before ->

    DataItem.lazyCache = {}
    

  it "should load projects", (done) ->

    @dataCollection = new DataItem.Collection("/projects")
    @dataCollection.fetchNext().then =>
      @dataCollection.length.should.be.at.least(1)
      done()


  it "should lazy load users", (done) ->

    @dataCollection = new DataItem.Collection("/projects")
    @dataCollection.fetchNext().then =>
      @dataCollection.get("0/participants/0/user", this, (value) ->
        chai.expect(value).not.to.be.undefined
        done()
      )

