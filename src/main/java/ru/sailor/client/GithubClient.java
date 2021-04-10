package ru.sailor.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import ru.sailor.converter.GithubBranchToGitCommitConverter;
import ru.sailor.converter.GithubCommitToGitCommitConverter;
import ru.sailor.data.GitBranch;
import ru.sailor.data.GitCommit;
import ru.sailor.data.GithubBranch;
import ru.sailor.data.GithubCommit;
import ru.sailor.data.GithubRepo;
import ru.sailor.exceptions.ApiRateLimitException;
import ru.sailor.exceptions.BranchNotFoundException;
import ru.sailor.exceptions.CommitNotFoundException;
import ru.sailor.exceptions.GitCommunicationException;
import ru.sailor.exceptions.InvalidAuthTokenException;
import ru.sailor.exceptions.InvalidCommitCountException;
import ru.sailor.exceptions.RepositoryNotFoundException;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GithubClient implements GitClient {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String GITHUB_API_URL = "https://api.github.com/repos";
    public static final Integer MAX_COMMITS_PER_PAGE = 100;
    private final CloseableHttpClient client = HttpClients.createDefault();
    private Boolean githubConnectionCheck;

    private final String githubUrl;
    private final Boolean hasToken;
    private final String authToken;

    public GithubClient(String owner, String repo) {
        githubUrl = GITHUB_API_URL + "/" + owner + "/" + repo;
        authToken = "";
        hasToken = false;
        githubConnectionCheck = false;
    }

    public GithubClient(String owner, String repo, String authToken) {
        githubUrl = GITHUB_API_URL + "/" + owner + "/" + repo;
        this.authToken = "Bearer " + authToken;
        hasToken = true;
        githubConnectionCheck = false;
    }

    public Collection<GitCommit> getAllCommitHistory(String commitSHA) throws GitCommunicationException {
        checkGithubConnection();

        Integer pageCount = 1;
        var commitHistory = new ArrayList<>(getPreviousCommits(commitSHA, pageCount, MAX_COMMITS_PER_PAGE));

        while (!hasTheFirstCommit(commitHistory)) {
            pageCount++;
            commitHistory.addAll(getPreviousCommits(commitSHA, pageCount, MAX_COMMITS_PER_PAGE));
        }

        return commitHistory.stream()
                .map(GithubCommitToGitCommitConverter::toGit)
                .collect(Collectors.toList());
    }

    public List<GitCommit> getCommitHistory(String commitSHA, Integer countOfCommits) throws GitCommunicationException {
        checkGithubConnection();

        if (countOfCommits < 0) {
            throw new InvalidCommitCountException("Count of commits should be > 0, " + countOfCommits + " < 0");
        }

        var commitHistory = new ArrayList<GithubCommit>();
        Integer pageCount = 1;
        while (countOfCommits > 0) {
            var previousCommits = getPreviousCommits(commitSHA, pageCount, Math.min(countOfCommits, MAX_COMMITS_PER_PAGE));
            if (previousCommits.isEmpty())
                break;

            commitHistory.addAll(previousCommits);

            countOfCommits -= Math.min(countOfCommits, MAX_COMMITS_PER_PAGE);
            pageCount++;

            if (hasTheFirstCommit(commitHistory))
                break;
        }

        return commitHistory.stream()
                .map(GithubCommitToGitCommitConverter::toGit)
                .collect(Collectors.toList());
    }

    public GitBranch getBranchInfo(String branchName) throws GitCommunicationException {
        checkGithubConnection();

        var request = new HttpGet(branchInfoUri(branchName));
        if (hasToken)
            request.addHeader(HttpHeaders.AUTHORIZATION, authToken);

        String responseBody;
        GithubBranch branchInfo;
        try {
            responseBody = client.execute(request, httpResponse ->
                    IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8.name()));
            branchInfo = mapper.readValue(responseBody, GithubBranch.class);
        } catch (IOException e) {
            throw new GitCommunicationException("Unknown error while making request to github");
        }

        if (branchInfo.getErrorMessage() != null) {
            throw new BranchNotFoundException("Branch not found");
        }

        return GithubBranchToGitCommitConverter.toGit(branchInfo);
    }

    private List<GithubCommit> getPreviousCommits(String commitSHA, Integer pageCount, Integer countOfCommits) throws GitCommunicationException {
        var request = new HttpGet(commitHistoryUri(commitSHA, pageCount, Math.min(countOfCommits, MAX_COMMITS_PER_PAGE)));

        if (hasToken)
            request.addHeader(HttpHeaders.AUTHORIZATION, authToken);

        String responseBody;
        List<GithubCommit> commits;
        try {
            responseBody = client.execute(request, httpResponse ->
                    IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8.name()));
            //todo delete or change to some logger
            System.out.println("Call commit history " + commitSHA + " " + pageCount);
            commits = mapper.readValue(responseBody, new TypeReference<ArrayList<GithubCommit>>() {});
        } catch (MismatchedInputException e) {
            //todo check anonymous request limit
//            e.printStackTrace();
            throw new CommitNotFoundException("Unknown error while making request to github");
        } catch (IOException e) {
            throw new GitCommunicationException("Unknown error while making request to github");
        }

        return commits;
    }

    private Boolean hasTheFirstCommit(List<GithubCommit> commits) {
        return commits.get(commits.size() - 1).getParents().size() == 0;
    }

    @SneakyThrows
    private URI commitHistoryUri(String commitSHA, Integer pageNumber, Integer countOnPage) {
        return new URIBuilder(githubUrl + "/commits")
                .addParameter("sha", commitSHA)
                .addParameter("per_page", countOnPage.toString())
                .addParameter("page", pageNumber.toString())
                .build();
    }

    @SneakyThrows
    private URI branchInfoUri(String branchName) {
        return new URI(githubUrl + "/branches/" + branchName);
    }


    private void checkGithubConnection() throws GitCommunicationException {
        var request = new HttpGet(githubUrl);
        if (hasToken)
            request.addHeader(HttpHeaders.AUTHORIZATION, authToken);

        String responseBody;
        GithubRepo repoInfo;
        try {
            responseBody = client.execute(request, httpResponse ->
                    IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8.name()));
            repoInfo = mapper.readValue(responseBody, GithubRepo.class);
        } catch (IOException e) {
            throw new GitCommunicationException("Unknown error while making request to github");
        }

        checkGithubErrorMessage(repoInfo);
    }

    private void checkGithubErrorMessage(GithubRepo repoInfo) throws GitCommunicationException {
        if (repoInfo.getErrorMessage() != null && repoInfo.getErrorMessage().equals("Bad credentials")) {
            throw new InvalidAuthTokenException("Bad credentials. Invalid auth token.");
        }
        if (repoInfo.getErrorMessage() != null && repoInfo.getErrorMessage().contains("API rate limit exceeded")) {
            throw new ApiRateLimitException("API rate limit exceeded. Try to use non anonymous calls.");
        }
        if (repoInfo.getErrorMessage() != null) {
            throw new RepositoryNotFoundException("Repository not found. Invalid owner or repo name");
        }
    }

}
