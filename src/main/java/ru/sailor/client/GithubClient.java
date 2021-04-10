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
import ru.sailor.exceptions.BranchNotFoundException;
import ru.sailor.exceptions.CommitNotFoundException;
import ru.sailor.exceptions.GitCommunicationException;
import ru.sailor.exceptions.InvalidCommitCountException;
import ru.sailor.exceptions.InvalidAuthTokenException;
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

    private final String githubUrl;
    private final Boolean hasToken;
    private final String authToken;

    public GithubClient(String owner, String repo) throws GitCommunicationException {
        githubUrl = GITHUB_API_URL + "/" + owner + "/" + repo;
        authToken = "";
        hasToken = false;
        checkGithubData();
    }

    public GithubClient(String owner, String repo, String authToken) throws GitCommunicationException {
        githubUrl = GITHUB_API_URL + "/" + owner + "/" + repo;
        if (authToken == null || authToken.isEmpty()) {
            throw new InvalidAuthTokenException("Token is empty");
        }
        this.authToken = "Bearer " + authToken;
        hasToken = true;

        checkGithubData();
    }

    public Collection<GitCommit> getAllCommitHistory(String commitSHA) throws GitCommunicationException {
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
        if (countOfCommits < 0) {
            throw new InvalidCommitCountException("Count of commits should be > 0, " + countOfCommits + " < 0");
        }
        var commitHistory = new ArrayList<GithubCommit>();
        Integer pageCount = 1;

        while (countOfCommits > 0) {
            var previousCommits = getPreviousCommits(commitSHA, pageCount, Math.min(countOfCommits, MAX_COMMITS_PER_PAGE));
            if(previousCommits.isEmpty())
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
        var request = new HttpGet(branchInfoUri(branchName));
        if (hasToken)
            request.addHeader(HttpHeaders.AUTHORIZATION, authToken);

        try{
            var responseBody = client.execute(request, httpResponse ->
                    IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8.name()));
            var branchInfo = mapper.readValue(responseBody, GithubBranch.class);
            if(branchInfo.getErrorMessage() != null) {
                throw new BranchNotFoundException("Branch not found");
            }

            return GithubBranchToGitCommitConverter.toGit(branchInfo);
        } catch (IOException e) {
            throw new GitCommunicationException(e.getMessage());
        }
    }


    private void checkGithubData() throws GitCommunicationException {
        var request = new HttpGet(githubUrl);
        if (hasToken)
            request.addHeader(HttpHeaders.AUTHORIZATION, authToken);

        try {
            var responseBody = client.execute(request, httpResponse ->
                    IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8.name()));
            var repoInfo = mapper.readValue(responseBody, GithubRepo.class);
            if (repoInfo.getMessage() != null && repoInfo.getMessage().equals("Bad credentials")) {
                throw new InvalidAuthTokenException("Bad credentials. Invalid auth token.");
            }
            if (repoInfo.getMessage() != null) {
                throw new RepositoryNotFoundException("Repository not found. Invalid owner or repo name");
            }
        } catch (IOException e) {
            throw new GitCommunicationException("Unknown error while making request to github");
        }
    }

    private Collection<GithubCommit> getPreviousCommits(String commitSHA, Integer pageCount, Integer countOfCommits) throws GitCommunicationException {
        var request = new HttpGet(commitHistoryUri(commitSHA, pageCount, Math.min(countOfCommits, MAX_COMMITS_PER_PAGE)));

        if (hasToken)
            request.addHeader(HttpHeaders.AUTHORIZATION, authToken);

        try{
            var responseBody = client.execute(request, httpResponse ->
                    IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8.name()));
            System.out.println("Call branch history " + commitSHA + " " + pageCount);
            return mapper.readValue(responseBody, new TypeReference<ArrayList<GithubCommit>>(){});
        } catch (MismatchedInputException e) {
            throw new CommitNotFoundException("Commit not found");
        }
        catch (IOException e) {
            throw new GitCommunicationException(e.getMessage());
        }
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

}
