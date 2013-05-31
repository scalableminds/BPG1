### define
jquery : $
underscore : _
d3 : d3
###

class Layouter

	constructor : () ->

    CLUSTER_POSITIONS =
      0 : "left"
      1 : "right"
      2 : "bottom"
      3 : "lr"
      4 : "lb"
      5 : "br"
      6 : "middle"
      7 : "no_cluster"

    PROJECT_SIZE = 64
    PADDING = 5


  textWrap : (svg_text, content, width) ->

    if svg_text? and content?

      t_copy = _.clone(svg_text)

      pos_x = parseInt( d3.select(svg_text).attr("x") )
      pos_y = parseInt( d3.select(svg_text).attr("y") )

      abc = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
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


  arrangeNodesInVenn : (nodeClusters) ->

    for c in nodeClusters
      cluster = nodeClusters[c]

    # projectClusters =
    #   "left" : []
    #   "right" : []
    #   "bottom" : []
    #   "lr" : []
    #   "lb" : []
    #   "br" : []
    #   "middle" : []
    #   "no_cluster" : []







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





