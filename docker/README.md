# docker build image and run the conatiner
**This docuemntation is in progress and subjected to change with the addition of new configuration files**
Your current direcotry should be pointing to ***cloudfeeds-repose-filters/docker*** 
***Copy a valid saxon license file to current directory***
>Setup Jfrog repository login for pulling Repose docker image.
$docker login https://repose-docker-local.artifacts.rackspace.net
>Enter your credentials

Passing value of the **schema_version** and **feeds_filters_version** **saxon_lic** argument is mandatory for successful build. This defines version for *usage-schema* and *custom filters*
Run the following command to build an image by providing the *schema_version* and **feeds_filters_version** value. 
```
$docker build --build-arg schema_version=[schema_version] --build-arg feeds_filters_version=[feeds_filters_version] --build-arg saxon_lic=[saxon_license_file] -f Dockerfile -t repose-valve:9.1.0.2 . 
```
Use the following command to run a repose-valve container on port 9090
```
$docker run -itd --name [Conatiner_Name] -p 9090:9090 repose-valve:9.1.0.2
```

Test with *curl -v http://localhost:9090*

Following environment variables are set 
```
SAXON_HOME=/etc/saxon
APP_ROOT=/etc/repose
APP_VARS=/var/repose
APP_LOGS=/var/log/repose
```

Example of build and run a container with schema version 1.137.0 and feeds_filters_version 1.6.0 with a valid saxon license file
```
$docker build --build-arg schema_version=1.137.0 --build-arg feeds_filters_version=1.6.0 --build-arg saxon_lic=saxon-licese.lic -t repose-valve:9.1.0.2 -f Dockerfile .
$docker run -itd --name repose-valve -p 9090:9090 repose-valve:9.1.0.2
```
