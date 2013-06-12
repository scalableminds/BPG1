### define
underscore : _
./exec_queue : ExecQueue
###

RangeSwitch = (execQueue, rules) ->

  if arguments.length == 1
    rules = execQueue
    execQueue = ExecQueue()

  functionBody = [
    "var args = [].slice.call(arguments, 2);"
  ]

  functions = []
  appendFunction = (func) ->

    functions.push(func)
    functionBody.push("  execQueue(function() { functions[#{functions.length - 1}].apply(undefined, args) });")

  _.forOwn(rules, (func, rule) ->

    [rule, a0, cmp0, cmp1, a1] = rule.match(/^([\d\.]+)\s*([<>=]+)\s*x\s*([<>=]+)\s*([\d\.]+)$/)

    functionBody.push("if (#{a0} #{cmp0} value && value #{cmp1} #{a1}) {")
    
    if _.isArray(func)
      func.forEach(appendFunction)
    else
      appendFunction(func)
    
    functionBody.push("}")
    return
  )

  compiledFunction = new Function("functions", "execQueue", "value", functionBody.join("\n"))

  (value, args...) ->
    compiledFunction(functions, execQueue, value, args...)