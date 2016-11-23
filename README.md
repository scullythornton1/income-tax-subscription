Income Tax Subscription MicroService
====================================

This MicroService provides Third Party Applications with RESTful APIs to allow a User to Subscribe for the Making Tax Digital - Income Tax (MTD-IT) Service.
 
## API Live Endpoints
It currently supports the following end-points: 
 
#### GET /income-tax-subscription/hello-world

This is an open resource without any authorisation required. It needs an 'Accept' header on the request with the following:
```
Accept: application/vnd.hmrc.1.0+json
```

request: 
```
GET /income-tax-subscription/hello-world
```
response:
```
{
    "message":"Hello World"
}
```


#### GET /income-tax-subscription/hello-application
This resource requires an application token as well as the 'Accept' header.
request: 
```
GET /income-tax-subscription/hello-application
```
response:
```
{
    "message":"Hello Application"
}
```


#### GET /income-tax-subscription/hello-user
This resource requires user token as well as the 'Accept' header.
request: 
```
GET /income-tax-subscription/hello-user
```
response:
```
{
    "message":"Hello User"
}
```

## API Sandbox Endpoints
All the above endpoints are accessible on sandbox with `sandbox` included in the URL of each endpoint,e.g.
```
    GET /income-tax-subscription/sandbox/hello-world
    GET /income-tax-subscription/sandbox/hello-application
    GET /income-tax-subscription/sandbox/hello-user
```

## API Definition
API definition for the service will be available under `/income-tax-subscription/api/definition` endpoint.
See definition in `/conf/api-definition.json` for the format.

## API Version
Version of API is 1.0 and needs to be provided in `Accept` request header
```
Accept: application/vnd.hmrc.1.0+json
```