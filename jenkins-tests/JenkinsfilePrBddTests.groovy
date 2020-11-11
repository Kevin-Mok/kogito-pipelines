import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification
import hudson.plugins.git.GitSCM

class JenkinsfilePrBddTests extends JenkinsPipelineSpecification {
	def Jenkinsfile = null

    def params = []
    def changeAuthor = 'user'
    def changeBranch = 'user-branch'

    def setup() {
        Jenkinsfile = loadPipelineScriptForTest('Jenkinsfile.pr-bdd-tests')

        explicitlyMockPipelineVariable('githubscm')
        Jenkinsfile.getBinding().setVariable('params', params)
        Jenkinsfile.getBinding().setVariable('changeAuthor', changeAuthor)
        Jenkinsfile.getBinding().setVariable('changeBranch', changeBranch)
    }

	def '[JenkinsfilePrBddTests.groovy] getPRRepoName' () {
		setup:
            Jenkinsfile.getBinding().setVariable('env', ['ghprbGhRepository' : 'foo/bar'])
		when:
			def repoName = Jenkinsfile.getPRRepoName()
		then:
            repoName == 'bar'
	}

	def '[JenkinsfilePrBddTests.groovy] addAuthorBranchParamsIfExist: exists' () {
		setup:
            getPipelineMock('githubscm.getRepositoryScm')('repo', changeAuthor, changeBranch) >> 'repo'
		when:
			Jenkinsfile.addAuthorBranchParamsIfExist(params, 'repo')
		then:
            1 * getPipelineMock('string.call').call(['name' : 'GIT_AUTHOR', 'value' : changeAuthor])
            1 * getPipelineMock('string.call').call(['name' : 'BUILD_BRANCH_NAME', 'value' : changeBranch])
	}

	def '[JenkinsfilePrBddTests.groovy] addAuthorBranchParamsIfExist: doesn\'t exist' () {
		setup:
            getPipelineMock('githubscm.getRepositoryScm')('kogito-examples', changeAuthor, changeBranch) >> null
		when:
			Jenkinsfile.addAuthorBranchParamsIfExist(params, 'repo')
		then:
            0 * getPipelineMock('string.call').call(['name' : 'GIT_AUTHOR', 'value' : changeAuthor])
            0 * getPipelineMock('string.call').call(['name' : 'BUILD_BRANCH_NAME', 'value' : changeBranch])
	}

	/* def '[JenkinsfilePrBddTests.groovy] addExamplesParamsForOperator: exists' () {
		setup:
            getPipelineMock('githubscm.getRepositoryScm')('kogito-examples', changeAuthor, changeBranch) >> 'repo'
		when:
			Jenkinsfile.addExamplesParamsForOperator(params)
		then:
            1 * getPipelineMock('string.call').call(['name' : 'EXAMPLES_URI', 'value' : "https://github.com/${changeAuthor}/kogito-examples"])
            1 * getPipelineMock('string.call').call(['name' : 'EXAMPLES_REF', 'value' : changeBranch])
	}

	def '[JenkinsfilePrBddTests.groovy] addExamplesParamsForOperator: doesn\'t exist' () {
		setup:
            getPipelineMock('githubscm.getRepositoryScm')('kogito-examples', changeAuthor, changeBranch) >> null
		when:
			Jenkinsfile.addExamplesParamsForOperator(params)
		then:
            1 * getPipelineMock('string.call').call(['name' : 'EXAMPLES_URI', 'value' : 'https://github.com/kiegroup/kogito-examples'])
            1 * getPipelineMock('string.call').call(['name' : 'EXAMPLES_REF', 'value' : 'master'])
	} */
}
