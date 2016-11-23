API Example Microservice
========================

This is a sample API to test API platform. It has 3 endpoints one for each level of authorisation.
 
## api-example-microservice
This is open resource without any authorisation
request: 
```
GET /hello/world
```
response:
```
{
    "message":"Hello World"
}
```
In the definition `authType` should be set to `NONE`
```
"authType": "NONE"
 ```   
## hello-application
This resource requires application token
request: 
```
GET /hello/application
```
response:
```
{
    "message":"Hello Application"
}
```
In the definition `authType` should be set to `APPLICATION`
```
"authType": "APPLICATION"
 ```   
## hello-user
This resource requires user token
request: 
```
GET /hello/user
```
response:
```
{
    "message":"Hello User"
}
```
In the definition `authType` should be `USER`
```
"authType": "USER"
 ```   

# Sandbox
All the above endpoints are accessible on sandbox with `/sandbox` prefix on each endpoint,e.g.
```
    GET /sandbox/hello/world
    GET /sandbox/hello/application
    GET /sandbox/hello/user
```

# Definition
API definition for the service will be available under `/api/definition` endpoint.
See definition in `/conf/api-definition.json` for the format.

# Version
Version of API need to be provided in `Accept` request header
```
Accept: application/vnd.hmrc.v1.0+json
```