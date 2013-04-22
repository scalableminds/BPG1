### define
jquery : $
###

class Layouter

	constructor : () ->

	    @graph = []

	newNodeWasSet : (newNode) ->

		occlusionEval(node, newNode) for node in @graph.nodes

	occlusionEval : (node, newNode) ->

		occludesNode = true if node.x

		moveNode newNode if occludesNode



		# Assignment:
		number   = 42
		opposite = true

		# Conditions:
		number = -42 if opposite

		square = (x) -> x * x
		cube   = (x) -> square(x) * x