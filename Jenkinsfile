node('java') {
   stage('Preparation') { // for display purpose
      git 'https://github.com/rackerlabs/cloudfeeds-repose-filters.git'
      // Get the Maven tool.
      // ** NOTE: This 'M3' Maven tool must be configured
   }
   stage('Builds') {
      // Run the maven build
     // sh "'${mvnHome}/bin/mvn' clean install" 
      sh "mvn clean install" 
   }
   stage('Results') {
      archiveArtifacts artifacts: "feeds-filters/target/feeds-filters*.ear"
      slackSend channel: "@teja.cheruku", color: "#FC05DE", message: "Deployed branch"
   }
}
