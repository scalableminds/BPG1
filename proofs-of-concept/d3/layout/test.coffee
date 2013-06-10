### define
d3 : d3
jquery : $
###

$ = jQuery

NODE_SIZE = 64

list_of_nodes = [
  {x: 50, y: 30},
  {x: 100, y: 500},
  {x: 120, y: 300},
  {x: 600, y: 200},
  {x: 200, y: 90},
  {x: 100, y: 90},
  {x: 500, y: 50},
  {x: 400, y: 200},
  {x: 200, y: 600},
  {x: 130, y: 400}
]

color = d3.scale.category10()

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

  not ((Math.abs(node.x - other_node.x) > NODE_SIZE) or
    (Math.abs(node.y - other_node.y) > NODE_SIZE))


distance_vector = (node, other_node) ->
# requires both nodes to have x and y attributes

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

###

# drag.on(type, listener)

Registers the specified listener to receive events of the specified type from the drag behavior. The following events are supported:

"dragstart": fired when a drag gesture is started.
"drag": fired when the element is dragged. d3.event will contain "x" and "y" properties representing the current absolute drag coordinates of the element. It will also contain "dx" and "dy" properties representing the element's coordinates relative to its position at the beginning of the gesture.
"dragend": fired when the drag gesture has finished.
# drag.origin([origin])

If origin is specified, sets the origin accessor to the specified function. If origin is not specified, returns the current origin accessor which defaults to null.

###

drag = d3.behavior.drag()
.on("drag", (d, i) ->
  console.log d, i
  d.x += d3.event.dx
  d.y += d3.event.dy
  d3.select(this).attr "transform", (d, i) ->
    "translate(" + [d.x, d.y] + ")"
)


draw_nodes = (svg) ->

  console.log "drawing"
  console.log list_of_nodes

  node = svg.selectAll(".node")
  .data(list_of_nodes)
  .enter().append("g")
  .attr("class", "node")
  .call(drag)
  .attr("transform", (d) ->
    "translate(" + d.x + "," + d.y + ")"
  )
  node.append("rect")
  .attr(
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
    )
  .attr("transform", (d) ->
    "translate(" + d.x-NODE_SIZE + "," + d.y-NODE_SIZE + ")"
  )
  .text((d, i) -> i)






