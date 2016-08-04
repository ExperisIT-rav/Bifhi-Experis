/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.beanconfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.autonomy.frontend.configuration.Authentication;
import com.hp.autonomy.frontend.configuration.BCryptUsernameAndPassword;
import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.configuration.ConfigurationFilterMixin;
import com.hp.autonomy.frontend.configuration.SingleUserAuthenticationValidator;
import com.hp.autonomy.frontend.find.hod.configuration.HodAuthenticationMixins;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.hod.parametricfields.CacheableIndexFieldsService;
import com.hp.autonomy.frontend.find.hod.parametricfields.CacheableParametricValuesService;
import com.hp.autonomy.frontend.find.hod.search.HodFindDocument;
import com.hp.autonomy.frontend.view.hod.HodViewService;
import com.hp.autonomy.frontend.view.hod.HodViewServiceImpl;
import com.hp.autonomy.hod.caching.HodApplicationCacheResolver;
import com.hp.autonomy.hod.client.api.analysis.autocomplete.AutocompleteService;
import com.hp.autonomy.hod.client.api.analysis.autocomplete.AutocompleteServiceImpl;
import com.hp.autonomy.hod.client.api.analysis.viewdocument.ViewDocumentService;
import com.hp.autonomy.hod.client.api.analysis.viewdocument.ViewDocumentServiceImpl;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationService;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationServiceImpl;
import com.hp.autonomy.hod.client.api.authentication.EntityType;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.resource.ResourcesService;
import com.hp.autonomy.hod.client.api.resource.ResourcesServiceImpl;
import com.hp.autonomy.hod.client.api.textindex.query.content.GetContentService;
import com.hp.autonomy.hod.client.api.textindex.query.content.GetContentServiceImpl;
import com.hp.autonomy.hod.client.api.textindex.query.fields.RetrieveIndexFieldsService;
import com.hp.autonomy.hod.client.api.textindex.query.fields.RetrieveIndexFieldsServiceImpl;
import com.hp.autonomy.hod.client.api.textindex.query.parametric.GetParametricValuesService;
import com.hp.autonomy.hod.client.api.textindex.query.parametric.GetParametricValuesServiceImpl;
import com.hp.autonomy.hod.client.api.textindex.query.search.Document;
import com.hp.autonomy.hod.client.api.textindex.query.search.FindRelatedConceptsService;
import com.hp.autonomy.hod.client.api.textindex.query.search.FindRelatedConceptsServiceImpl;
import com.hp.autonomy.hod.client.api.textindex.query.search.FindSimilarService;
import com.hp.autonomy.hod.client.api.textindex.query.search.FindSimilarServiceImpl;
import com.hp.autonomy.hod.client.api.textindex.query.search.QueryTextIndexService;
import com.hp.autonomy.hod.client.api.textindex.query.search.QueryTextIndexServiceImpl;
import com.hp.autonomy.hod.client.api.userstore.user.UserStoreUsersService;
import com.hp.autonomy.hod.client.api.userstore.user.UserStoreUsersServiceImpl;
import com.hp.autonomy.hod.client.config.HodServiceConfig;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxyService;
import com.hp.autonomy.hod.client.token.TokenRepository;
import com.hp.autonomy.hod.databases.DatabasesService;
import com.hp.autonomy.hod.databases.DatabasesServiceImpl;
import com.hp.autonomy.hod.databases.ResourceMapper;
import com.hp.autonomy.hod.databases.ResourceMapperImpl;
import com.hp.autonomy.hod.fields.IndexFieldsService;
import com.hp.autonomy.hod.fields.IndexFieldsServiceImpl;
import com.hp.autonomy.hod.parametricvalues.HodParametricValuesService;
import com.hp.autonomy.hod.sso.HodAuthenticationRequestService;
import com.hp.autonomy.hod.sso.HodAuthenticationRequestServiceImpl;
import com.hp.autonomy.hod.sso.SpringSecurityTokenProxyService;
import com.hp.autonomy.hod.sso.UnboundTokenService;
import com.hp.autonomy.hod.sso.UnboundTokenServiceImpl;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
@EnableCaching
public class HodConfiguration extends CachingConfigurerSupport {
    public static final String SSO_PAGE_PROPERTY = "${find.hod.sso:https://www.idolondemand.com/sso.html}";
    public static final String HOD_API_URL_PROPERTY = "${find.iod.api:https://api.havenondemand.com}";
    public static final String SIMPLE_CACHE_RESOLVER_NAME = "simpleCacheResolver";

    @Autowired
    private Environment environment;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ConfigFileService<HodFindConfig> configService;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Bean
    @Autowired
    public ObjectMapper jacksonObjectMapper(final Jackson2ObjectMapperBuilder builder) {
        return builder.createXmlMapper(false)
                .mixIn(Authentication.class, HodAuthenticationMixins.class)
                .mixIn(BCryptUsernameAndPassword.class, ConfigurationFilterMixin.class)
                .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }

    @Bean
    public SingleUserAuthenticationValidator singleUserAuthenticationValidator() {
        final SingleUserAuthenticationValidator singleUserAuthenticationValidator = new SingleUserAuthenticationValidator();
        singleUserAuthenticationValidator.setConfigService(configService);

        return singleUserAuthenticationValidator;
    }

    @Override
    @Bean
    public CacheResolver cacheResolver() {
        final HodApplicationCacheResolver hodApplicationCacheResolver = new HodApplicationCacheResolver();
        hodApplicationCacheResolver.setCacheManager(cacheManager);

        return hodApplicationCacheResolver;
    }

    // Resolver for caches which are not application-specific
    @Bean(name = SIMPLE_CACHE_RESOLVER_NAME)
    public CacheResolver simpleCacheResolver() {
        final SimpleCacheResolver resolver = new SimpleCacheResolver();
        resolver.setCacheManager(cacheManager);
        return resolver;
    }

    @Bean
    public HttpClient httpClient() {
        final HttpClientBuilder builder = HttpClientBuilder.create();

        final String proxyHost = environment.getProperty("find.https.proxyHost");

        if (proxyHost != null) {
            final Integer proxyPort = Integer.valueOf(environment.getProperty("find.https.proxyPort", "8080"));
            builder.setProxy(new HttpHost(proxyHost, proxyPort));
        }

        builder.disableCookieManagement();

        return builder.build();
    }

    private HodServiceConfig.Builder<EntityType.Combined, TokenType.Simple> hodServiceConfigBuilder() {
        final String endpoint = environment.getProperty("find.iod.api", "https://api.havenondemand.com");

        return new HodServiceConfig.Builder<EntityType.Combined, TokenType.Simple>(endpoint)
                .setHttpClient(httpClient())
                .setTokenRepository(tokenRepository);
    }

    @Bean
    public HodServiceConfig<EntityType.Combined, TokenType.Simple> initialHodServiceConfig() {
        return hodServiceConfigBuilder()
                .build();
    }

    @Bean
    public AuthenticationService authenticationService() {
        return new AuthenticationServiceImpl(initialHodServiceConfig());
    }

    @Bean
    public TokenProxyService<EntityType.Combined, TokenType.Simple> tokenProxyService() {
        return new SpringSecurityTokenProxyService();
    }

    @Bean
    public HodServiceConfig<EntityType.Combined, TokenType.Simple> hodServiceConfig() {
        return hodServiceConfigBuilder()
                .setTokenProxyService(tokenProxyService())
                .build();
    }

    @Bean
    public ResourcesService resourcesService() {
        return new ResourcesServiceImpl(hodServiceConfig());
    }

    @Bean
    public QueryTextIndexService<Document> documentQueryTextIndexService() {
        return QueryTextIndexServiceImpl.documentsService(hodServiceConfig());
    }

    @Bean
    public QueryTextIndexService<HodFindDocument> queryTextIndexService() {
        return new QueryTextIndexServiceImpl<>(hodServiceConfig(), HodFindDocument.class);
    }

    @Bean
    public FindRelatedConceptsService relatedConceptsService() {
        return new FindRelatedConceptsServiceImpl(hodServiceConfig());
    }

    @Bean
    public RetrieveIndexFieldsService retrieveIndexFieldsService() {
        return new RetrieveIndexFieldsServiceImpl(hodServiceConfig());
    }

    @Bean
    public IndexFieldsService indexFieldsService() {
        return new CacheableIndexFieldsService(new IndexFieldsServiceImpl(retrieveIndexFieldsService()));
    }

    @Bean
    public DatabasesService databasesService() {
        return new DatabasesServiceImpl(resourcesService(), resourceMapper());
    }

    @Bean
    public ResourceMapper resourceMapper() {
        return new ResourceMapperImpl(indexFieldsService());
    }

    @Bean
    public GetParametricValuesService getParametricValuesService() {
        return new GetParametricValuesServiceImpl(hodServiceConfig());
    }

    @Bean
    public CacheableParametricValuesService parametricValuesService() {
        return new CacheableParametricValuesService(new HodParametricValuesService(getParametricValuesService()));
    }

    @Bean
    public GetContentService<Document> getContentService() {
        return GetContentServiceImpl.documentsService(hodServiceConfig());
    }

    @Bean
    public ViewDocumentService viewDocumentService() {
        return new ViewDocumentServiceImpl(hodServiceConfig());
    }

    @Bean
    public HodAuthenticationRequestService hodAuthenticationRequestService() {
        return new HodAuthenticationRequestServiceImpl(configService, authenticationService(), unboundTokenService());
    }

    @Bean
    public UnboundTokenService<TokenType.HmacSha1> unboundTokenService() {
        try {
            return new UnboundTokenServiceImpl(authenticationService(), configService);
        } catch (final HodErrorException e) {
            throw new BeanInitializationException("Exception creating UnboundTokenService", e);
        }
    }

    @Bean
    public HodViewService hodViewService() {
        return new HodViewServiceImpl(viewDocumentService(), getContentService(), documentQueryTextIndexService());
    }

    @Bean
    public FindSimilarService<HodFindDocument> findSimilarService() {
        return new FindSimilarServiceImpl<>(hodServiceConfig(), HodFindDocument.class);
    }

    @Bean
    public UserStoreUsersService userStoreUsersService() {
        return new UserStoreUsersServiceImpl(hodServiceConfig());
    }

    @Bean
    public AutocompleteService autocompleteService() {
        return new AutocompleteServiceImpl(hodServiceConfig());
    }
}
