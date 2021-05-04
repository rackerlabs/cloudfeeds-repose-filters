# docker build image and run the conatiner
**This docuemntation is in progress and subjected to change with the addition of new configuration files**
Your current direcotry should be pointing to ***cloudfeeds-repose-filters/docker*** 
***Copy a valid saxon license file to the current directory also need VPN and Jfrog Repository access***
>Setup Jfrog repository login for pulling Repose docker image.
$docker login https://repose-docker-local.artifacts.rackspace.net
>Enter your credentials

***Mandatory arguments:*** 
**schema_version** - Schema version to be used
**feeds_filters_version** - Custom filters version
**saxon_lic** - saxon license file
***Optional argument:***
**repose_valve** - CloudFeeds Repose image type. Possible values: *common* (custom filters with common configuration), *internal* or *external*. 

Following environment variables are set 
```
SAXON_HOME=/etc/saxon
APP_ROOT=/etc/repose
APP_VARS=/var/repose
APP_LOGS=/var/log/repose
DESTINATION_PORT=8080
```

Run the following command to build an image with custom filters only. 
```
docker build --build-arg schema_version=1.137.0 --build-arg feeds_filters_version=1.7.0 --build-arg saxon_lic=saxon-license.lic -f Dockerfile -t cloudfeeds-repose-custom:9.1.0.2 . 
```

Run the following command to build an image with custom + external filters. 
```
docker build --build-arg schema_version=1.137.0 --build-arg feeds_filters_version=1.7.0 --build-arg saxon_lic=saxon-license.lic --build-arg repose_valve=external -f Dockerfile -t cloudfeeds-repose-external:9.1.0.2 . 
```

Use the following command to run a cloudfeeds repose-valve external container on port 9090
```
$docker run -itd --name [Conatiner_Name] -p 9090:9090 cloudfeeds-repose-external:9.1.0.2
```

Test with *curl -v http://localhost:9090*

Envrionment varaible *DESTINATION_PORT* is configured with 8080 by default. To connect with a different destination port pass the environment variable value at runtime.
Sample command to cofigure 8081 as destination port.
```
$docker run -itd --name [Conatiner_Name] -p 9090:9090 --env DESTINATION_PORT=8081 cloudfeeds-repose-external:9.1.0.2
```