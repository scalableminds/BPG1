### define
underscore : _
###

ExecQueue = (wrapper = (f) -> -> f()) ->

  queue = []
  isRunning = false

  exec = (args...) ->
    queue.push(args...)
    unless isRunning

      isRunning = true
      next = wrapper ->

        if queue.length > 0
          queue.shift()()
          next()

        else
          isRunning = false

      next()

    return