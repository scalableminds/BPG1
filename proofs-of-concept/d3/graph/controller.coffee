define [
    "graph"
  ],
  ( graph ) ->

    class Controller

      WIDTH  = 960
      HEIGHT = 500

      constructor : ->

        @nodes = [
          {id: 0, reflexive: false},
          {id: 1, reflexive: true },
          {id: 2, reflexive: false}
        ]

        @links = [
          {source: @nodes[0], target: @nodes[1], left: false, right: true }
          {source: @nodes[1], target: @nodes[2], left: false, right: true }
        ]

        @lastNodeId = 2


        @svg = d3.select("body")
          .append("svg")
          .attr("WIDTH", WIDTH)
          .attr("HEIGHT", HEIGHT)
          .attr("pointer-events", "all")


        #need for touch/mouse events for zooming
        @svg.append('svg:g')
        @hitbox = @svg.append('svg:rect')
          .attr('width', WIDTH)
          .attr('height', HEIGHT)
          .attr('fill', 'white')
          .call(
            d3.behavior.zoom()
              .on("zoom", ( => @redraw()) )
          )
        @zoomGroup = @svg.append('svg:g')

        @hitbox
          .on("mousedown", @mousedown)
          .on("mousemove", @mousemove)
          .on("mouseup", @mouseup)

        @graph = new graph(@zoomGroup, WIDTH, HEIGHT)
        @graph.setNodes( @nodes )
        @graph.setLinks( @links )
        @graph.init()

        # line displayed when dragging new nodes
        @drag_line = @zoomGroup.append("svg:path")
          .attr("class", "link dragline hidden")
          .attr("d", "M0,0L0,0")



      mousedown : =>

        { mousedown_node, mousedown_link } = @graph

        # because :active only works in WebKit?
        @graph.svg.classed("active", true)

        return if d3.event.ctrlKey or mousedown_node or mousedown_link

        # insert new node at point
        __this = @svg[0][0]
        point = d3.mouse(__this)
        node =
          id: ++@lastNodeId
          reflexive: false

        node.x = point[0]
        node.y = point[1]
        node.fixed = true
        @nodes.push(node)

        @graph.restart()

      mousemove : =>
        { mousedown_node } = @graph
        unless mousedown_node
          @drag_line
            .classed("hidden", true)
          return

        # update drag line
        __this = @svg[0][0]
        @drag_line
        @drag_line
          .classed("hidden", false)
          .style('marker-end', 'url(#end-arrow)')
          .attr("d", "M#{mousedown_node.x},#{mousedown_node.y}L#{d3.mouse(__this)[0]},#{d3.mouse(__this)[1]}")

        #@restart()

      mouseup : =>
        { mousedown_node } = @graph

        if mousedown_node
          # hide drag line
          @drag_line
            .classed("hidden", true)
            .style("marker-end", "")

        # because :active only works in WebKit?
        @graph.svg.classed("active", false)

        # clear mouse event s
        @graph.resetMouseVars()


      redraw : ->
        @zoomGroup.attr("transform","translate(#{d3.event.translate}) scale(#{d3.event.scale})")




      # d3.select(window)
      #   .on("keydown", @keydown)
      #   .on("keyup", @keyup)

    #   keydown : =>

    #   { selected_node, selected_link } = this

    #   # ctrl
    #   if d3.event.keyCode == 17
    #     @circle.call(@force.drag)
    #     @svg.classed("ctrl", true)


    #   return unless selected_node and selected_link
    #   switch d3.event.keyCode
    #     when 46 #delete
    #       if selected_node
    #         nodes.splice(nodes.indexOf(selected_node), 1)
    #         spliceLinksForNode(selected_node)
    #       else if selected_link
    #         links.splice(links.indexOf(selected_link), 1)

    #       @selected_link = null
    #       @selected_node = null
    #       @restart()

    #     when 66 # B
    #       if selected_link
    #         # set link direction to both left and right
    #         selected_link.left = true
    #         selected_link.right = true

    #       @restart()


    # keyup : =>
    #   # ctrl
    #   if d3.event.keyCode == 17
    #     @circle
    #       .on("mousedown.drag", null)
    #       .on("touchstart.drag", null)
    #     @svg.classed("ctrl", false)


      #     spliceLinksForNode : (node) ->
      # toSplice = links.filter(
      #   (l) ->
      #     return (l.source == node or l.target == node)
      # )

      # toSplice.map( (l) ->
      #   links.splice(links.indexOf(l), 1)
      # )


