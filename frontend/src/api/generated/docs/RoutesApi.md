# RoutesApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**createRoute**](#createroute) | **POST** /api/routes | Create a new saved route|
|[**listRoutes**](#listroutes) | **GET** /api/routes | List all saved routes|

# **createRoute**
> SavedRouteResponse createRoute(savedRouteRequest)

Creates a new saved route with origin and destination

### Example

```typescript
import {
    RoutesApi,
    Configuration,
    SavedRouteRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new RoutesApi(configuration);

let savedRouteRequest: SavedRouteRequest; //

const { status, data } = await apiInstance.createRoute(
    savedRouteRequest
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **savedRouteRequest** | **SavedRouteRequest**|  | |


### Return type

**SavedRouteResponse**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | Route created successfully |  -  |
|**400** | Invalid request |  -  |
|**401** | Unauthorized |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **listRoutes**
> Array<SavedRouteResponse> listRoutes()

Returns all saved routes for the authenticated user

### Example

```typescript
import {
    RoutesApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new RoutesApi(configuration);

const { status, data } = await apiInstance.listRoutes();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<SavedRouteResponse>**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | List of saved routes |  -  |
|**401** | Unauthorized |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

