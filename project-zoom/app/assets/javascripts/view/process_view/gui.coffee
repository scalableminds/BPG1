### define
jquery : $
###

class GUI

  constructor : ->

    @initNavbar()


  initNavbar : ->

    $('.navbar li').on "click", (event) ->
      $('.navbar li').removeClass('active')

      $this = $(@)
      unless $this.hasClass('active')
        $this.addClass('active')

      event.preventDefault()


