### define
d3 : d3
jquery : $
###

$ = jQuery

NODE_SIZE = 64

list_of_nodes = [
  {x: 50, y: 30},
  {x: 100, y: 50},
  {x: 120, y: 30},
  {x: 600, y: 200},
  {x: 200, y: 90},
  {x: 100, y: 90},
  {x: 500, y: 50},
  {x: 400, y: 200},
  {x: 200, y: 30},
  {x: 130, y: 40}
]

color = d3.scale.category10()
node_drag = d3.behavior.drag()
  .on("drag", dragmove)


$(document).ready( ->
  try_this()
)

###

Functions:

###

try_this = ->

  svg = d3.select("body").append("svg")
    .attr("width", 1000)
    .attr("height", 1000)
    .attr("id", "svg")
    .append("g")

  draw_nodes(svg)

  node =
    x: 10
    y: 20

  node_image = svg.append("rect")
  .attr(
    x: node.x
    y: node.y
    width: NODE_SIZE
    height: NODE_SIZE
    rx: 20
    ry: 20
    opacity: .5
    )
  .style(
    fill: "forestgreen"
    stroke: "red"
    "stroke-width": 5
    )

  colls = get_collisions(node, list_of_nodes)
  console.log colls


collides_with = (node, other_node) ->
  console.log Math.abs(node.x - other_node.x)
  console.log Math.abs(node.y - other_node.y)

  if (not ((Math.abs(node.x - other_node.x) > NODE_SIZE) or (Math.abs(node.y - other_node.y) > NODE_SIZE)))
    console.log "true"
  else
    console.log "false"

  console.log "################################"
  not ((Math.abs(node.x - other_node.x) > NODE_SIZE) or (Math.abs(node.y - other_node.y) > NODE_SIZE))


distance_vector = (node, other_node) ->
# requires bothe nodes to have x and y attributes

  x = other_node.x - node.x
  y = other_node.y - node.y

  {x, y}


get_collisions = (curr_node, other_nodes) ->

  collisions = []

  console.log curr_node

  for node, i in other_nodes
    console.log i, node
    if collides_with(curr_node, node)
      console.log "coll!!!!"
      collisions.push node

  collisions


move_current_node = (curr_node, new_x, new_y) ->

  curr_node.x = new_x
  curr_node.y = new_y


move = ->

  dragTarget = d3.select(this)
  dragTarget.attr("cx", ->
    d3.event.dx + parseInt(dragTarget.attr("cx"))
  ).attr "cy", ->
    d3.event.dy + parseInt(dragTarget.attr("cy"))


dragmove = (d, i) ->

  console.log "dragging"

  d.x = d3.event.x
  d.y = d3.event.y
  d3.select(this)
    .attr("transform", (d) -> "translate(" + d.x + "," + d.y + ")")


draw_nodes = (svg) ->

  console.log "drawing"
  console.log list_of_nodes

  node = svg.selectAll(".node")
  .data(list_of_nodes)
  .enter().append("g")
  .attr("class", "node")
  # .call(node_drag)
  # .attr("transform", (d) ->
  #   "translate(" + d.x + "," + d.y + ")"
  # )
  node.append("rect")
  .attr(
    x: (d) -> d.x
    y: (d) -> d.y
    width: NODE_SIZE
    height: NODE_SIZE
    rx: 20
    ry: 20
  )
  .style "fill", (d, i) ->
    color i % 10
  node.append("text")
  .attr(
    "font-size": 80
    x: (d) -> d.x + NODE_SIZE/2
    y: (d) -> d.y + NODE_SIZE/2
    )
  .text((d, i) -> i)






