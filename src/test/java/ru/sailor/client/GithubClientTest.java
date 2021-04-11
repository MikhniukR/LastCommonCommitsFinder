package ru.sailor.client;

import org.junit.Assert;
import org.junit.Test;
import ru.sailor.data.GitCommit;
import ru.sailor.exceptions.ApiRateLimitException;
import ru.sailor.exceptions.GitCommunicationException;
import ru.sailor.exceptions.InvalidAuthTokenException;
import ru.sailor.exceptions.InvalidCommitCountException;
import ru.sailor.exceptions.DataNotFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

public class GithubClientTest {

    private final String authToken = "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";

    @Test(expected = DataNotFoundException.class)
    public void testInvalidRepoName() throws GitCommunicationException {
        var githubClient = new GithubClient("MikhniukR", "InvalidRepo", authToken);
        githubClient.getBranchInfo("master");
    }

    @Test(expected = InvalidAuthTokenException.class)
    public void testInvalidAuthToken() throws GitCommunicationException {
        var githubClient = new GithubClient("MikhniukR", "LastCommonCommitsFinder", "invalidAuthToken");
        githubClient.getBranchInfo("master");
    }

    @Test
    public void testsGetOneCommitWithoutToken() throws GitCommunicationException {
        var githubClient = new GithubClient("MikhniukR", "LastCommonCommitsFinder");

        try {
            Assert.assertEquals(1, githubClient.getCommitHistory("3f491e8e2ba5f17630e0c1cbef2aed7427c78fbf", 1).size());
        }
        catch (ApiRateLimitException ignored) {

        }
    }

    @Test(expected = ApiRateLimitException.class)
    public void testsGetAnonymousLimit() throws GitCommunicationException {
        var githubClient = new GithubClient("Microsoft", "git");

        githubClient.getCommitHistory("c087afa24a08843c93e4e92810e521f9a0cd01af", 120_000);
    }

    @Test
    public void testsGetOneCommit() throws GitCommunicationException {
        var githubClient = new GithubClient("MikhniukR", "LastCommonCommitsFinder", authToken);

        Assert.assertEquals(1, githubClient.getCommitHistory("3f491e8e2ba5f17630e0c1cbef2aed7427c78fbf", 1).size());
    }

    @Test
    public void testsGetOneCommitBody() throws GitCommunicationException {
        var githubClient = new GithubClient("MikhniukR", "LastCommonCommitsFinder", authToken);

        Assert.assertEquals(GitCommit.builder()
                        .sha("3f491e8e2ba5f17630e0c1cbef2aed7427c78fbf")
                        .timestamp(LocalDateTime.of(2021, 4, 9, 17, 45, 39))
                        .parents(Collections.emptyList())
                        .build(),
                githubClient.getCommitHistory("3f491e8e2ba5f17630e0c1cbef2aed7427c78fbf", 1).get(0));
    }

    @Test
    public void testsGetTwoCommits() throws GitCommunicationException {
        var githubClient = new GithubClient("MikhniukR", "LastCommonCommitsFinder", authToken);

        Assert.assertEquals(2, githubClient.getCommitHistory("2acdbe2955b815e07f8420ac510236416344c043", 2).size());
    }

    @Test
    public void testGetTenCommits() throws GitCommunicationException {
        var githubClient = new GithubClient("MikhniukR", "MikhniukR.github.io", authToken);

        Assert.assertEquals(10, githubClient.getCommitHistory("92794c0461f1f89b296851fc907d6d859fc0faba", 10).size());
    }

    @Test
    public void testMaxOnPageCommits() throws GitCommunicationException {
        var githubClient = new GithubClient("Microsoft", "git", authToken);
        var cntOfCommits = GithubClient.MAX_COMMITS_PER_PAGE;

        Assert.assertEquals(cntOfCommits.intValue(), githubClient.getCommitHistory("c087afa24a08843c93e4e92810e521f9a0cd01af", cntOfCommits).size());

    }

    @Test
    public void testGetTenPagesOfCommits() throws GitCommunicationException {
        var githubClient = new GithubClient("Microsoft", "git", authToken);
        var cntOfCommits = 10 * GithubClient.MAX_COMMITS_PER_PAGE;

        Assert.assertEquals(cntOfCommits, githubClient.getCommitHistory("c087afa24a08843c93e4e92810e521f9a0cd01af", cntOfCommits).size());

    }

    @Test
    public void testGetHundredPagesOfCommits() throws GitCommunicationException {
        var githubClient = new GithubClient("Microsoft", "git", authToken);
        var cntOfCommits = 100 * GithubClient.MAX_COMMITS_PER_PAGE;

        Assert.assertEquals(cntOfCommits, githubClient.getCommitHistory("c087afa24a08843c93e4e92810e521f9a0cd01af", cntOfCommits).size());

    }

    @Test
    public void testGetUniqCommits() throws GitCommunicationException {
        var githubClient = new GithubClient("Microsoft", "git", authToken);
        var result = githubClient.getCommitHistory("c807f771947de65dceb22960d1a093d702f42105", GithubClient.MAX_COMMITS_PER_PAGE * 3);
        var uniqResult = Set.copyOf(result);

        Assert.assertEquals(uniqResult.size(), result.size());
    }

    @Test
    public void testGetThousandCommits() throws GitCommunicationException {
        var githubClient = new GithubClient("Microsoft", "git", authToken);

        Assert.assertEquals(1_000, githubClient.getCommitHistory("bbca20accefe8329f976371d0d8c111c965cdf1a", 1_000).size());
    }

    @Test(expected = DataNotFoundException.class)
    public void testGetCommitsInvalidCommitHash() throws GitCommunicationException {
        var githubClient = new GithubClient("Microsoft", "git", authToken);
        githubClient.getCommitHistory("invalidCommitHash", 1);
    }

    @Test(expected = InvalidCommitCountException.class)
    public void testGetCommitsInvalidCommitCount() throws GitCommunicationException {
        var githubClient = new GithubClient("Microsoft", "git", authToken);
        githubClient.getCommitHistory("a7336ae514738f159dad314d6674961427f043a6", -100);
    }

    @Test
    public void testGetBranchInfoSHA() throws GitCommunicationException {
        var githubClient = new GithubClient("MikhniukR", "Test_db", authToken);

        Assert.assertEquals("099a79ac0581230cec78cdbdff5b581d6ef9105f", githubClient.getBranchInfo("master").getLastCommit().getSha());
    }

    @Test(expected = DataNotFoundException.class)
    public void testGetBranchInfoInvalidBranch() throws GitCommunicationException {
        var githubClient = new GithubClient("MikhniukR", "Test_db", authToken);

        githubClient.getBranchInfo("invalid_branch");
    }

}