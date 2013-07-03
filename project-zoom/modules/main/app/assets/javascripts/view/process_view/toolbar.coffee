### define
jquery : $
text!templates/process_view_toolbar.html : ToolbarTemplate
###

class Toolbar

	constructor : ->

		@$el = $(ToolbarTemplate)
		@el = @$el[0]


	activate : ->

    @$el.find(".toolbar .btn").on "click", (event) =>
      @$el.find(".toolbar .btn").removeClass('active')

      $this = $(event.target)
      unless $this.hasClass('active')
        $this.addClass('active')

      event.preventDefault()


  deactivate : ->

  	@$el.find(".toolbar .btn").off("click")
