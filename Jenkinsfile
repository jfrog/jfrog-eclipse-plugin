node('java') {
    cleanWs()
    git url: 'https://github.com/jfrog/jfrog-eclipse-plugin.git'
    def jdktool = tool name: "1.8.0_102"
    env.JAVA_HOME = jdktool
    echo jdktool
    def release = RELEASE_PLUGIN && VERSION != "" && NEXT_DEVELOPMENT_VERSION != ""
    def rtServer, rtMaven, buildInfo

    stage('Artifactory configuration') {
        rtServer = Artifactory.server('oss.jfrog.org')
        buildInfo = Artifactory.newBuildInfo()
        buildInfo.name = 'EcoSystem :: jfrog-eclipse-plugin'
        rtMaven = Artifactory.newMavenBuild()
        rtMaven.deployer releaseRepo: 'oss-release-local', snapshotRepo: 'oss-snapshot-local', server: rtServer
        rtMaven.resolver releaseRepo: 'remote-repos', snapshotRepo: 'remote-repos', server: rtServer
        rtMaven.tool = 'mvn-3.3.9'
    }

    if (release) {
        stage('Check Bintray credentials') {
            sh '''#!/bin/bash 
                set -o pipefail
                curl -so /dev/null -w "Bintray response: %{http_code}" https://api.bintray.com/repos/jfrog/jfrog-eclipse-plugin -u $BINTRAY_USER:$BINTRAY_API_KEY --fail 2>&1 | grep -v https
            '''
        }
        
        stage('Check GitHub credentials') {
            sh '''#!/bin/bash 
                set -o pipefail
                curl -so /dev/null -w "GitHub response: %{http_code}" https://api.github.com -u $GITHUB_USERNAME:$GITHUB_PASSWORD --fail 2>&1 | grep -v https
            '''
        }
        
        stage('Set release version') {
            rtMaven.run pom: 'pom.xml', goals: 'clean org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$VERSION', buildInfo: buildInfo
        }
    }

    stage('Build Eclipse plugin') {
        rtMaven.run pom: 'pom.xml', goals: '-V -B -U clean install', buildInfo: buildInfo
    }

    stage('Publish build info') {
        rtServer.publishBuildInfo(buildInfo)
    }

    if (!release) {
        return
    }

    stage('Commit and create version tag') {
        sh("git commit --allow-empty -am '[artifactory-release] Release version ${VERSION}'")
        sh("git tag '${VERSION}'")
    }

    pushToGithub()

    stage('Distribute') {
        dir ('releng/update-site') {
            sh "./gradlew publishP2Repo"
        }
    }

    stage('Set next development version') {
        rtMaven.deployer.deployArtifacts = false
        rtMaven.run pom: 'pom.xml', goals: 'clean org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$NEXT_DEVELOPMENT_VERSION', buildInfo: buildInfo
        sh("git commit -am '[artifactory-release] Next development version'")
    }

    pushToGithub()
}

def pushToGithub() {
    stage('Push changes') {
        sh '''#!/bin/bash 
            set -o pipefail
            git push https://${GITHUB_USERNAME}:${GITHUB_PASSWORD}@github.com/JFrog/jfrog-eclipse-plugin.git 2>&1 | grep -v "http"
            git push https://${GITHUB_USERNAME}:${GITHUB_PASSWORD}@github.com/JFrog/jfrog-eclipse-plugin.git --tags 2>&1 | grep -v "http"
        '''
    }
}
