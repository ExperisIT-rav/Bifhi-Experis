define([
    'backbone',
    'js-whatever/js/list-view',
    'js-whatever/js/list-item-view',
    'find/app/model/search-filters-collection',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filter-display/filter-display.html',
    'text!find/templates/app/page/search/filter-display/filter-display-item.html',
    'bootstrap'
], function(Backbone, ListView, ListItemView, SearchFiltersCollection, i18n, template, itemTemplate) {

    var FilterListItemView = ListItemView.extend({
        render: function() {
            ListItemView.prototype.render.apply(this, arguments);

            this.$tooltip = this.$('[data-toggle="tooltip"]');

            this.$tooltip.tooltip({
                container: 'body',
                placement: 'bottom'
            });
        },

        remove: function() {
            this.$tooltip.tooltip('destroy');

            ListItemView.prototype.remove.apply(this, arguments);
        }
});

    // Each of the collection's models should have an id and a text attribute
    return Backbone.View.extend({
        template: _.template(template),
        itemTemplate: _.template(itemTemplate),

        events: {
            'click .filters-remove-icon': function(e) {
                var id = $(e.currentTarget).closest('[data-id]').attr('data-id');
                var metaType = $(e.currentTarget).closest('[data-metatype]').attr('data-metatype');
                var type = $(e.currentTarget).closest('[data-type]').attr('data-type');

                this.removeFilter(id, metaType, type);
            },

            'click .remove-all-filters': function() {
                // Separate picking attributes from calling removeFilter so we don't modify the collection while iterating
                _.chain(this.collection.models)
                    .map(function(model) {
                        return model.pick('id', 'metaType', 'type');
                    })
                    .each(function(attributes) {
                        this.removeFilter(attributes.id, attributes.metaType, attributes.type);
                    }, this);
            }
        },

        initialize: function(options) {
            this.datesFilterModel = options.datesFilterModel;

            this.listView = new ListView({
                collection: this.collection,
                ItemView: FilterListItemView,
                className: 'inline-block',
                itemOptions: {
                    className: 'label filter-label border filters-margin inline-block m-b-xs',
                    template: this.itemTemplate
                }
            });

            this.listenTo(this.collection, 'reset update', this.updateVisibility);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.updateVisibility();

            this.listView.render();
            this.$('.filters-labels').append(this.listView.$el);

            return this;
        },

        updateVisibility: function() {
            this.$el.toggleClass('hide', this.collection.isEmpty());
        },

        removeFilter: function(id, metaType, type) {
            if (metaType && metaType === SearchFiltersCollection.metaFilterTypes.date) {
                if (type === SearchFiltersCollection.FilterTypes.dateRange) {
                    this.datesFilterModel.setDateRange(null);
                }
                if (type === SearchFiltersCollection.FilterTypes.minDate) {
                    this.datesFilterModel.setMinDate(null);
                }
                if (type === SearchFiltersCollection.FilterTypes.maxDate) {
                    this.datesFilterModel.setMaxDate(null);
                }
            } else {
                this.collection.remove(id);
            }
        }
    });

});