### define
d3 : d3
lib/event_mixin : EventMixin
./graph : Graph
./behavior/connect_nodes_behavior : ConnectNodesBehavior
./behavior/drag_node_behavior : DragNodeBehavior
###

class InteractiveGraph extends Graph

  constructor : (@container, @svg) ->

    EventMixin.extend(this)

    @currentBehavior = new DragNodeBehavior(@container, @svg, @)
    @currentBehavior.active()

    super(@container)











