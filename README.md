# MSX Swagger 

## Overview

This is a standalone library that provide OpenAPI 2.0 support. 
It also includes a customized Swagger UI with support of MSX Single-Sign-On. 

## Default Properties

> **Note**: There has been update on default swagger properties. Only the listed properties are supported.

```
swagger.enabled=true

swagger.security.sso.enabled=true
swagger.security.sso.baseUrl=http://localhost:9103/idm
swagger.security.sso.tokenPath=/v2/token
swagger.security.sso.authorizePath=/v2/authorize
swagger.security.sso.clientId=
swagger.security.sso.clientSecret=

swagger.ui.enabled=true
swagger.ui.endpoint=/swagger
swagger.ui.view=/swagger-ui.html

swagger.security.oauth2.enabled=false
swagger.security.oauth2.baseUrl=http://localhost:9103/idm
swagger.security.oauth2.tokenPath=/v2/token
swagger.security.oauth2.authorizePath=/v2/authorize
```

## Swagger UI

When enabled, the Swagger UI can be loaded at:

```
http://<service.domain>/<service-context-path>/swagger
```

The UI would attempt to authenticate by using MSX's SSO page in a pop-up window. Popup blocker need to be disabled for 
SSO to work. 

## Deployment Notes

To use the Swagger UI in deployment environment, operators need to override two properties in consul:

1. For each service, update SSO authorization/token URL:
 
    ```
    swagger.security.sso.base-url=http://<service.domain>/idm
    ```

2. For `usermanagementservice` whitelisted redirect URL need to be configured properly:
 
    ```
    security.auth.white-listed-redirect-url=<http or https>://<service.domain>/**/swagger-sso-redirect.html,...
    ``` 

## Build
There are two ways to build the project:
1. Install the local node and npm per the versions specified by the properties `node.version` and `npm.version` defined in the pom.xml and use them to build the frontend.
   It is needed when the build environment has not installed the global node and npm: 
    ```
    mvn clean install
    ```
   or 
    ```
    mvn clean install -P local-node-npm
    ```
   The default profile is `use-local-node-npm`.
2. The build environment has installed the global node and npm and it is intended to use them to build the frontend:
    ```
    mvn clean install -P global-node-npm
    ```
