### define
jquery : $
###

class GUI

  constructor : ->

    @initToolbar()

    # $("svg").on "mousedown", "img", (evt) ->
    #   return false;


  initToolbar : ->

    $('.btn-group .btn').on "click", (event) ->
      $('.btn-group .btn').removeClass('active')

      $this = $(@)
      unless $this.hasClass('active')
        $this.addClass('active')

      event.preventDefault()


