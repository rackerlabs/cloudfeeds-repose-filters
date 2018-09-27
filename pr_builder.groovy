node('java') {
    //def mvnHome
    stage('Preparation') { // for display purposes
       // Get some code from a GitHub repository
       git 'https://github.com/rackerlabs/cloudfeeds-repose-filters.git'
       // Get the Maven tool.
       // ** NOTE: This 'M3' Maven tool must be configured
       // **       in the global configuration.           
      // mvnHome = tool 'M3'
    }
    stage('Build') {
       // Run the maven build
       sh 'mvn clean install'
    }
    stage('Results') {
       archiveArtifacts artifacts: "feeds-filters/target/feeds-filters*.ear"
    }
 }
