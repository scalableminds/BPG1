define ->

  class Graph

    colors = d3.scale.category10()

    constructor : (svg_container, @width, @height) ->

      @svg = svg_container.append("svg:g")


    setNodes : (@nodes) ->


    setLinks : (@links) ->


    init : ->

            #var force = d3.layout.force().gravity(.05).distance(100).charge(-100).size([width, height + 300]);
      # init D3 force layout
      @force = d3.layout.force()
        .nodes(@nodes)
        .links(@links)
        .size([@width, @height])
        .linkDistance(150)
        .charge(-500)
        .on("tick", ( => @tick()) )

      @initArrowMarkers()


      # handles to link and node element groups
      @path = @svg.append("svg:g").selectAll("path")
      @circle = @svg.append("svg:g").selectAll("g")

      # mouse event s
      @selected_node = null
      @selected_link = null
      @mousedown_link = null
      @mousedown_node = null
      @mouseup_node = null

      @restart()


    initArrowMarkers : ->

      # define arrow markers for graph links
      @svg.append("svg:defs").append("svg:marker")
          .attr("id", "end-arrow")
          .attr("viewBox", "0 -5 10 10")
          .attr("refX", 6)
          .attr("markerWidth", 3)
          .attr("markerHeight", 3)
          .attr("orient", "auto")
        .append("svg:path")
          .attr("d", "M0,-5L10,0L0,5")
          .attr("fill", "#000")

      @svg.append("svg:defs").append("svg:marker")
          .attr("id", "start-arrow")
          .attr("viewBox", "0 -5 10 10")
          .attr("refX", 4)
          .attr("markerWidth", 3)
          .attr("markerHeight", 3)
          .attr("orient", "auto")
        .append("svg:path")
          .attr("d", "M10,-5L0,0L10,5")
          .attr("fill", "#000")


    resetMouseVars : ->
        @mousedown_node = null
        @mouseup_node = null
        @mousedown_link = null


    # update force layout (called automatically each iteration)
    tick : ->
      # draw directed edges with proper padding from node centers
      @path.attr("d", (d) ->

        deltaX = d.target.x - d.source.x
        deltaY = d.target.y - d.source.y
        dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY)
        return if dist == 0

        normX = deltaX / dist
        normY = deltaY / dist
        sourcePadding = if d.left then 17 else 12
        targetPadding = if d.right then 17 else 12
        sourceX = d.source.x + (sourcePadding * normX)
        sourceY = d.source.y + (sourcePadding * normY)
        targetX = d.target.x - (targetPadding * normX)
        targetY = d.target.y - (targetPadding * normY)
        return "M#{sourceX},#{sourceY}L#{targetX},#{targetY}"
      )

      @circle.attr("transform", (d) ->
        return "translate(#{d.x},#{d.y})"
      )

    # update graph (called when needed)
    restart : ->

      { mousedown_node, mousedown_link, mouseup_node, selected_node, selected_link } = this

      # path (link) group
      @path = @path.data(@links)

      # update existing links
      @path
        .classed("selected", (d) ->
          return d == selected_link
        )
        .style("marker-start", (d) ->
          return if d.left then "url(#start-arrow)" else ""
        )
        .style("marker-end", (d) ->
          return if d.right then "url(#end-arrow)" else ""
        )

      # add new links
      @path.enter().append("svg:path")
        .attr("class", "link")
        .classed("selected", (d) ->
          return d == selected_link
        )
        .style("marker-start", (d) ->
          return if d.left then "url(#start-arrow)" else ""
        )
        .style("marker-end", (d) ->
          return if d.right then "url(#end-arrow)" else ""
        )
        .on("mousedown", (d) =>
          return if d3.event.ctrlKey

          # select link
          mousedown_link = d
          if mousedown_link == @selected_link
            @selected_link = null
          else
            @selected_link = mousedown_link

          @selected_node = null
          @restart()
        )

      # remove old links
      @path.exit().remove()


      # circle (node) group
      # NB: the function arg is crucial here! nodes are known by id, not by index!
      @circle = @circle.data(@nodes, (d) ->
        return d.id
      )

      # update existing nodes (reflexive & selected visual states)
      @circle.selectAll("circle")
        .style("fill", (d) ->
          return  if (d == selected_node) then d3.rgb(colors(d.id)).brighter().toString() else colors(d.id)
        )
        .classed("reflexive", (d) ->
          return d.reflexive
        )

      # add new nodes
      g = @circle.enter().append("svg:g")

      g.append("svg:circle")
        .attr("class", "node")
        .attr("r", 12)
        .style("fill", (d) ->
          return if (d == selected_node) then d3.rgb(colors(d.id)).brighter().toString() else colors(d.id)
        )
        .style("stroke", (d) ->
          return d3.rgb(colors(d.id)).darker().toString()
        )
        .classed("reflexive", (d) ->
          return d.reflexive
        )
        .on("mouseover", (d) ->
          if not mousedown_node or d == mousedown_node
            return
          # enlarge target node
          d3.select(this).attr("transform", "scale(1.1)")
        )
        .on("mouseout", (d) ->
          if not mousedown_node or d == mousedown_node
           return
          # unenlarge target node
          d3.select(this).attr("transform", "")
        )
        .on("mousedown", (d) =>
          return if d3.event.ctrlKey

          # select node
          mousedown_node = d
          if mousedown_node == selected_node
            @selected_node = null
          else
            @selected_node = mousedown_node

          @selected_link = null
          @mousedown_node = mousedown_node

          @restart()
        )
        .on("mouseup", (d) =>

          { mousedown_node, mouseup_node } = this

          return unless mousedown_node

          # check for drag-to-self
          mouseup_node = d
          if mouseup_node == mousedown_node
            @resetMouseVars()
            return

          # unenlarge target node
          __this = @svg[0][0]
          d3.select(__this).attr("transform", "")

          # add link to graph (update if exists)
          # NB: links are strictly source < target arrows separately specified by booleans
          if mousedown_node.id < mouseup_node.id
            source = mousedown_node
            target = mouseup_node
            direction = "right"
          else
            source = mouseup_node
            target = mousedown_node
            direction = "left"

          @mousedown_node = null

          link = @links.filter(
            (l) ->
              return (l.source == source and l.target == target)
          )[0]

          if link
            link[direction] = true
          else
            link =
              source: source
              target: target
              left: false
              right: false

            link[direction] = true
            @links.push(link)

          # select new link
          @selected_link = link
          @selected_node = null
          @restart()
        )

      # show node IDs
      g.append("svg:text")
          .attr("x", 0)
          .attr("y", 4)
          .attr("class", "id")
          .text((d)->
            return d.id
          )

      # remove old nodes
      @circle.exit().remove()

      # create a static graph
      @force.start()
      for i in [0..500]
        @force.tick()
      @force.stop()




