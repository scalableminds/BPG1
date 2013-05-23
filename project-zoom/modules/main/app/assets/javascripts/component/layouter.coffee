### define
jquery : $
###

class Layouter

  PROJECT_SIZE = 64
  PADDING = 5

	constructor : ->
    console.log "hi i'm the layouter"


  resizeCircle : (circle, weight) ->



  arrangeInSquare : (objects, square) ->
    start_x = square[0][0]
    start_y = square[0][1]

    len_x   = square[1][0]
    len_y   = square[1][1]

    object_width = PROJECT_SIZE

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

  arrangeNodesInClusters : (nodeClusters) ->







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





