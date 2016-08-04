/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/service-view',
    'find/hod/app/model/hod-search-filters-collection',
    'find/hod/app/page/search/filters/indexes/hod-indexes-view',
    'find/hod/app/page/search/results/hod-results-view'
], function(ServiceView, SearchFiltersCollection, IndexesView, ResultsView) {
    'use strict';

    return ServiceView.extend({
        constructSearchFiltersCollection: function (queryModel, datesFilterModel, indexesCollection, selectedIndexesCollection, selectedParametricValues) {
            return new SearchFiltersCollection([], {
                queryModel: queryModel,
                datesFilterModel: datesFilterModel,
                indexesCollection: indexesCollection,
                selectedIndexesCollection: selectedIndexesCollection,
                selectedParametricValues: selectedParametricValues
            });
        },

        constructIndexesView: function (queryModel, indexesCollection, selectedIndexesCollection) {
            return new IndexesView({
                queryModel: queryModel,
                indexesCollection: indexesCollection,
                selectedDatabasesCollection: selectedIndexesCollection
            });
        },

        constructResultsView: function (models) {
            return new ResultsView(models);
        }
    });

});
