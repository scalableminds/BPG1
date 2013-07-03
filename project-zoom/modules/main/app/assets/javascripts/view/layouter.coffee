### define
jquery : $
underscore : _
d3 : d3
###

class Layouter

	constructor : (@nodes, process_view=false) ->

    @cluster_positions = ["left", "right", "bottom", "lr", "lb", "br", "middle", "no_cluster"]

    if process_view is true
      @node_positions = @convert_nodes()

    @PROJECT_SIZE = 64
    PADDING = 5
    @MARGIN = 5


  convert_nodes : ->

    result = []
    nodes_objects = @nodes.toObject()

    for node in nodes_objects

      temp =
        x: node.position.x
        y: node.position.y

      result.push temp

    return result


  textWrap : (svg_text, content, width) ->

    if svg_text? and content?

      t_copy = _.clone(svg_text)

      pos_x = parseInt( d3.select(svg_text).attr("x") )
      pos_y = parseInt( d3.select(svg_text).attr("y") )

      abc = "abcdefghijklmnopqrstuvwxyzäöüßABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜ"
      t_copy.textContent = abc

      letterWidth = t_copy.getBBox().width / abc.length
      letterHeight = t_copy.getBBox().height

      words = content.split(" ")

      x = 0
      line_words = []

      for w in words
        l = w.length
        if x + (l * letterWidth) > width
          line_words.push "\n"
          x = 0
        x += l * letterWidth
        line_words.push w + " "

      svg_text.textContent = ""
      joined = line_words.join("")
      lines = joined.split("\n")

      for line, i in lines
        d3.select(svg_text).append("tspan")
        .text(line)
        .attr(
          x: pos_x
          y: pos_y + i * (letterHeight)
        )


  resizeCircle : (circle, weight) ->


  arrangeInSquare : (objects, square) ->

    start_x = square[0][0]
    start_y = square[0][1]

    len_x   = square[1][0]
    len_y   = square[1][1]

    object_width = @PROJECT_SIZE

    pos_x = start_x
    pos_y = start_y

    for o in objects
      o.moveNode(pos_x, pos_y)

      if (pos_x + PADDING + object_width) < (start_x + len_x)
        pos_x += PADDING + object_width

      else
        pos_x = start_x
        pos_y += PADDING + object_width


  getSquareInArea : (area) ->


  arrangeNodesInVenn : (nodeClusters) ->

    for p in @cluster_positions
      cluster = nodeClusters[p]

##############################

  collides_with : (node, other_node) ->

    not ((Math.abs(node.x - other_node.x) > @PROJECT_SIZE) or
      (Math.abs(node.y - other_node.y) > @PROJECT_SIZE))


  distance_vector : (node, other_node) ->
  # requires both nodes to have x and y attributes

    x = other_node.x - node.x
    y = other_node.y - node.y

    {x: x, y: y}


  reverse_vector : (vector) ->

    new_x = - vector.x
    new_y = - vector.y
    {x: new_x, y: new_y}


  add_vectors : (vector, other_vector) ->

    x = vector.x + other_vector.x
    y = vector.y + other_vector.y

    {x: x, y: y}


  subtract_vectors : (vector, other_vector) ->

    x = vector.x - other_vector.x
    y = vector.y - other_vector.y

    {x: x, y: y}


  overlap_vector : (node, other_node) ->

    temp = @subtract_vectors(node, other_node)

    x = if temp.x < 0 then temp.x + @PROJECT_SIZE else temp.x - @PROJECT_SIZE
    y = if temp.y < 0 then temp.y + @PROJECT_SIZE else temp.y - @PROJECT_SIZE

    {x: x, y: y}


  move_if_collision : (curr_node) ->


    # console.log "size:", Node(@nodes[0]).getSize().width

    no_collisions = false

    curr_node_position = {
      x: parseInt curr_node.get("position/x")
      y: parseInt curr_node.get("position/y")
    }

    console.log curr_node

    destination_vector = {x: 0, y: 0}
    dragged_node = curr_node_position

    loop_iterations = 0
    while (no_collisions is false) and (loop_iterations < 4)

      collisions = @get_collisions(curr_node_position, @node_positions, dragged_node)
      console.log "colls: ", collisions

      if collisions.length is 0
        no_collisions = true

      else if collisions.length >= 4
        # curr_node_position = origin
        no_collisions = true

      else
        reversed_dest_vector = {x: 0, y: 0}

        for coll_node in collisions
          overlap_vec = @overlap_vector(curr_node_position, coll_node)
          reversed_dest_vector = @add_vectors(reversed_dest_vector, overlap_vec)

        destination_vector = @reverse_vector reversed_dest_vector

        # add margin:
        destination_vector.x = if (destination_vector.x < 0) then destination_vector.x - @MARGIN else destination_vector.x + @MARGIN
        destination_vector.y = if (destination_vector.y < 0) then destination_vector.y - @MARGIN else destination_vector.y + @MARGIN

        curr_node_position = @add_vectors(curr_node_position, destination_vector)

        loop_iterations += 1

    curr_node_position


  get_collisions : (curr_node, other_nodes, curr_node_copy=null) ->

    collisions = []

    for node, i in _.without(other_nodes, curr_node, curr_node_copy)
      if @collides_with(curr_node, node)
        collisions.push node

    collisions






























#######################################

  snap : (value, gridSize, roundFunction) ->

    roundFunction = Math.round  if roundFunction is `undefined`
    gridSize * roundFunction(value / gridSize)








 #  drawVenn : (selectedTags, projects) ->
 #    data = []

 #    for p in projects
 #      tags = []
 #      for t in p.tags
 #        tags.push t.name if t.name in selectedTags

 #      data.push tags


 #    color = d3.scale.category10()
 #    venn = d3.layout.venn(data, 3).size([800, 600])

 #    circle = d3.svg.arc().innerRadius(0).startAngle(0).endAngle(2*Math.PI)

 #    vis = d3.select("#graph")
 #      .data([data])
 #    circles = vis.selectAll("g.arc").data(venn).enter().append("g").attr("class", "arc").attr("transform", (d, i) ->
 #      "translate(" + (50 + d.x) + "," + (50 + d.y) + ")"
 #    )

 #    window.debug = circles

 #    circles.append("path").attr("fill", (d, i) ->
 #      color i
 #    ).attr("opacity", 0.5).attr("d", circle)

 #    circles.append("text").attr("text-anchor", "middle").text((d, i) ->
 #      d.label
 #    ).attr("fill", (d, i) ->
 #      color i
 #    ).attr("x", (d, i) ->
 #      d.labelX
 #    ).attr "y", (d, i) ->
 #      d.labelY









	# # newNodeWasSet : (newNode) ->

	# # 	occlusionEval(node, newNode) for node in @graph.nodes

	# # occlusionEval : (node, newNode) ->

	# # 	occludesNode = true if node.x

	# # 	moveNode newNode if occludesNode





