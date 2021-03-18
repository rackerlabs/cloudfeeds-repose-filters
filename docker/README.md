# docker build image and run the conatiner
**This docuemntation is in progress and subjected to change with the addition of new configuration files**
Your current direcotry should be pointing to ***cloudfeeds-repose-filters*** 

>Setup Jfrog repository login for pulling Repose docker image.
$docker login https://repose-docker-local.artifacts.rackspace.net
>Enter your credentials

Passing value of the **schema_version** argument is mandatory for successful build. This defines version for *usage-schema*
Run the following command to build an image by providing the *schema_version* value. 
```
$docker build --build-arg schema_version=[schema_version] -f docker/Dockerfile -t repose-valve:9.1.0.0 . 
```
Use the following command to run a repose-valve container on port 9090
```
$docker run -itd --name [Conatiner_Name] -p 9090:9090 repose-valve:9.1.0.0
```

Test with *curl -v http://localhost:9090*

Following environment variables are set 
```
SAXON_HOME=/etc/saxon
APP_ROOT=/etc/repose
APP_VARS=/var/repose
APP_LOGS=/var/log/repose
```

Example of building an image with schema version 1.137.0  and running a container.
```
$docker build --build-arg schema_version=1.137.0 -t repose-valve:9.1.0.0 .
$docker run -itd --name repose-valve -p 9090:9090 repose-valve:9.1.0.0
```



