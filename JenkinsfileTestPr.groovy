import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification
import hudson.plugins.git.GitSCM

class JenkinsfileTestPr extends JenkinsPipelineSpecification {
	def Jenkinsfile = null

    def params = []
    def env = [:]

    def setup() {
        Jenkinsfile = loadPipelineScriptForTest("Jenkinsfile.test-pr")

        explicitlyMockPipelineVariable("githubscm")
        env['ghprbPullAuthorLogin'] = 'user'
        env['ghprbSourceBranch'] = 'user-branch'
        Jenkinsfile.getBinding().setVariable("params", params)
        Jenkinsfile.getBinding().setVariable("env", env)
    }

	def "[JenkinsfileTestPr.groovy] getPRRepoName" () {
		setup:
            Jenkinsfile.getBinding().setVariable("env", ['ghprbGhRepository' : 'foo/bar'])
		when:
			def repoName = Jenkinsfile.getPRRepoName()
		then:
            repoName == 'bar'
	}

	def "[JenkinsfileTestPr.groovy] addAuthorBranchParamsIfExist: exists" () {
		setup:
            getPipelineMock("githubscm.getRepositoryScm")('repo', 'user', 'user-branch') >> 'repo'
		when:
			Jenkinsfile.addAuthorBranchParamsIfExist(params, 'repo')
		then:
            1 * getPipelineMock("string.call").call(['name':'GIT_AUTHOR', 'value':'user'])
            1 * getPipelineMock("string.call").call(['name':'BUILD_BRANCH_NAME', 'value':'user-branch'])
	}

	def "[JenkinsfileTestPr.groovy] addAuthorBranchParamsIfExist: doesn't exist" () {
		setup:
            getPipelineMock("githubscm.getRepositoryScm")('kogito-examples', 'user', 'user-branch') >> null
		when:
			Jenkinsfile.addAuthorBranchParamsIfExist(params, 'repo')
		then:
            0 * getPipelineMock("string.call").call(['name':'GIT_AUTHOR', 'value':'user'])
            0 * getPipelineMock("string.call").call(['name':'BUILD_BRANCH_NAME', 'value':'user-branch'])
	}

	def "[JenkinsfileTestPr.groovy] addExamplesParamsForOperator: exists" () {
		setup:
            getPipelineMock("githubscm.getRepositoryScm")('kogito-examples', 'user', 'user-branch') >> 'repo'
		when:
			Jenkinsfile.addExamplesParamsForOperator(params)
		then:
            1 * getPipelineMock("string.call").call(['name':'EXAMPLES_REF', 'value':'user'])
            1 * getPipelineMock("string.call").call(['name':'EXAMPLES_URI', 'value':'user-branch'])
	}

	def "[JenkinsfileTestPr.groovy] addExamplesParamsForOperator: doesn't exist" () {
		setup:
            getPipelineMock("githubscm.getRepositoryScm")('kogito-examples', 'user', 'user-branch') >> null
		when:
			Jenkinsfile.addExamplesParamsForOperator(params)
		then:
            1 * getPipelineMock("string.call").call(['name':'EXAMPLES_REF', 'value':'kiegroup'])
            1 * getPipelineMock("string.call").call(['name':'EXAMPLES_URI', 'value':'master'])
	}
}
