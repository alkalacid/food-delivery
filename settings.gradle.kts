rootProject.name = "food-delivery-system"

// Infrastructure services
include("config-server")
include("discovery-server")
include("api-gateway")

// Business services
include("user-service")
include("restaurant-service")
include("order-service")
include("delivery-service")
include("payment-service")
include("notification-service")

// Common library
include("common-lib")

