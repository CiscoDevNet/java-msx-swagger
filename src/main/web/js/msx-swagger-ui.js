import csrfSupport from './csrf';
import {
  MsxOAuth2SsoPlugin
} from './plugins/sso-plugin'

window.onload = () => {

  const getBaseURL = () => {
    const urlMatches = /(.*)\/swagger.*/.exec(window.location.href);
    return urlMatches[1];
  };

  const getUI = (baseUrl, resources, configUI, oauthSecurity, ssoSecurity) => {

    let layout = "StandaloneLayout";
    let plugins = [ SwaggerUIBundle.plugins.DownloadUrl ];

    if (ssoSecurity) {
      layout = "SsoStandaloneLayout";
      plugins = [ SwaggerUIBundle.plugins.DownloadUrl, MsxOAuth2SsoPlugin ];
    }

    const ui = SwaggerUIBundle({
      /*--------------------------------------------*\
       * Core
      \*--------------------------------------------*/
      configUrl: null,
      dom_id: "#swagger-ui",
      dom_node: null,
      spec: {},
      url: "",
      urls: resources,
      /*--------------------------------------------*\
       * Plugin system
      \*--------------------------------------------*/
      initialState: { },
      layout: layout,
      plugins: plugins,
      presets: [
        SwaggerUIBundle.presets.apis,
        SwaggerUIStandalonePreset
      ],
      /*--------------------------------------------*\
       * Display
      \*--------------------------------------------*/
      deepLinking: configUI.deepLinking,
      displayOperationId: configUI.displayOperationId,
      defaultModelsExpandDepth: configUI.defaultModelsExpandDepth,
      defaultModelExpandDepth: configUI.defaultModelExpandDepth,
      defaultModelRendering: configUI.defaultModelRendering,
      displayRequestDuration: configUI.displayRequestDuration,
      docExpansion: configUI.docExpansion,
      filter: configUI.filter,
      maxDisplayedTags: configUI.maxDisplayedTags,
      operationsSorter: configUI.operationsSorter,
      showExtensions: configUI.showExtensions,
      tagSorter: configUI.tagSorter,
      /*--------------------------------------------*\
       * Network
      \*--------------------------------------------*/
      oauth2RedirectUrl: baseUrl + "/webjars/swagger-ui/oauth2-redirect.html",
      requestInterceptor: (a => a),
      responseInterceptor: (a => a),
      showMutatedRequest: true,
      supportedSubmitMethods: configUI.supportedSubmitMethods,
      validatorUrl: configUI.validatorUrl,
      /*--------------------------------------------*\
       * Macros
      \*--------------------------------------------*/
      modelPropertyMacro: null,
      parameterMacro: null,
    });

    oauthSecurity && ui.initOAuth({
      /*--------------------------------------------*\
       * OAuth
      \*--------------------------------------------*/
      clientId: oauthSecurity.clientId,
      clientSecret: oauthSecurity.clientSecret,
      realm: oauthSecurity.realm,
      appName: oauthSecurity.appName,
      scopeSeparator: oauthSecurity.scopeSeparator,
      additionalQueryStringParams: oauthSecurity.additionalQueryStringParams,
      useBasicAuthenticationWithAccessCodeGrant: oauthSecurity.useBasicAuthenticationWithAccessCodeGrant,
    });

    ssoSecurity && ui.initSso({
      /*--------------------------------------------*\
       * OAuth
      \*--------------------------------------------*/
      authorizeUrl: ssoSecurity.authorizeUrl,
      tokenUrl: ssoSecurity.tokenUrl,
      ssoRedirectUrl: baseUrl + "/swagger-sso-redirect.html",
      clientId: ssoSecurity.clientId,
      clientSecret: ssoSecurity.clientSecret,
    });

    return ui;
  };

  const buildSystemAsync = async (baseUrl) => {
    try {
      const configUIResponse = await fetch(
          baseUrl + "/swagger-resources/configuration/ui",
          {
            credentials: 'same-origin',
            headers: {
              'Accept': 'application/json',
              'Content-Type': 'application/json'
            },
          });
      const configUI = await configUIResponse.json();

      const configOAuth2SecurityResponse = await fetch(
          baseUrl + "/swagger-resources/configuration/security",
          {
            credentials: 'same-origin',
            headers: {
              'Accept': 'application/json',
              'Content-Type': 'application/json'
            },
          });
      const oauthSecurity = await configOAuth2SecurityResponse.json();

      const configSsoSecurityResponse = await fetch(
          baseUrl + "/swagger-resources/configuration/security/sso",
          {
            credentials: 'same-origin',
            headers: {
              'Accept': 'application/json',
              'Content-Type': 'application/json'
            },
          });
      const ssoSecurity = await configSsoSecurityResponse.json();

      const resourcesResponse = await fetch(
          baseUrl + "/swagger-resources",
          {
            credentials: 'same-origin',
            headers: {
              'Accept': 'application/json',
              'Content-Type': 'application/json'
            },
          });
      const resources = await resourcesResponse.json();
      resources.forEach(resource => {
        if (resource.url.substring(0, 4) !== 'http') {
          resource.url = baseUrl + resource.url;
        }
      });

      window.ui = getUI(baseUrl, resources, configUI, oauthSecurity, ssoSecurity);
    } catch (e) {
      const retryURL = await prompt(
        "Unable to infer base url. This is common when using dynamic servlet registration or when" +
        " the API is behind an API Gateway. The base url is the root of where" +
        " all the swagger resources are served. For e.g. if the api is available at http://example.org/api/v2/api-docs" +
        " then the base url is http://example.org/api/. Please enter the location manually: ",
        window.location.href);

      return buildSystemAsync(retryURL);
    }
  };

  /* Entry Point */
  (async () => {
    await buildSystemAsync(getBaseURL());
    // await csrfSupport(getBaseURL());
  })();

};
