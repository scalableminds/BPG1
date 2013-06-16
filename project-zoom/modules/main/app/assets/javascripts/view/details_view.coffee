### define
underscore : _
jquery : $
lib/event_mixin : EventMixin
hammer : Hammer
text!templates/details_view.html : DetailsViewTemplate
###

DetailsViewTemplate = _.template(DetailsViewTemplate)

class DetailsView


  constructor : (@projectModel) ->

    EventMixin.extend(this)

    @$el = $(DetailsViewTemplate(project : projectModel.toObject()))
    @el = @$el[0]

    @isActivated = false


  deactivate : ->

    return unless @isActivated

    

  activate : ->

    return if @isActivated
