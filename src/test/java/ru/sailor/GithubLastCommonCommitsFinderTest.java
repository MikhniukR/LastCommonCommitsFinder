package ru.sailor;

import org.junit.Assert;
import org.junit.Test;
import ru.sailor.exceptions.DataNotFoundException;
import ru.sailor.exceptions.InvalidAuthTokenException;

import java.io.IOException;
import java.util.List;

public class GithubLastCommonCommitsFinderTest {

    private final String authToken = "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";

    @Test(expected = DataNotFoundException.class)
    public void testInvalidRepoName() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "InvalidRepo", authToken);
        finder.findLastCommonCommits("main", "main");
    }

    @Test(expected = InvalidAuthTokenException.class)
    public void testInvalidAuthToken() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "Test_db", "invalidToken");
        finder.findLastCommonCommits("main", "main");
    }

    @Test(expected = DataNotFoundException.class)
    public void testInvalidBranchName() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "Test_db", authToken);
        finder.findLastCommonCommits("invalidBranch", "invalidBranch");
    }

    @Test
    public void testTheSameBranch() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "Test_db", authToken);
        var result = finder.findLastCommonCommits("master", "master");

        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.contains("099a79ac0581230cec78cdbdff5b581d6ef9105f"));
    }

    //    repo tree description
//    *   27deb8e (HEAD -> master, origin/master, origin/HEAD)
//    |\
//    | * 031e1da (origin/addTests)
//    * | 00c097d
//    |\|
//    | * cdc85a4
//    | * 28ecf16
//    |/
//    *   5016755
//    |\
//    | * 6177e1b(origin/useQuery)
//    |/
//    * 70738e6
    @Test
    public void testOneCommonBIsHigher() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "site-statistic", authToken);
        var result = finder.findLastCommonCommits("useQuery", "addTests");

        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.contains("6177e1b22fcb68622bb1c7c1b24c72d7459a94e5"));
    }

    @Test
    public void testOneCommonAIsHigher() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "site-statistic", authToken);
        var result = finder.findLastCommonCommits("addTests", "useQuery");

        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.contains("6177e1b22fcb68622bb1c7c1b24c72d7459a94e5"));
    }

    @Test
    public void testOneCommonAIsHigherWithMaster() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "site-statistic", authToken);
        var result = finder.findLastCommonCommits("master", "useQuery");

        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.contains("6177e1b22fcb68622bb1c7c1b24c72d7459a94e5"));
    }

    @Test
    public void testOneCommonAIsHigherBIsAParent() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "site-statistic", authToken);
        var result = finder.findLastCommonCommits("master", "addTests");

        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.contains("031e1da59d2c6d24e9a883f905fcbaaacfdf68e2"));
    }


    //    repo tree description
//    *   d58626e (HEAD -> master, origin/master, origin/HEAD)
//    |\
//    | * 21e8634 (origin/addIdeaFiles)
//    |/
//    *   ba204ab
//    |\
//    | * 8c26a21 (origin/fixApiTests)
//    |/
//    *   2d94208
//    |\
//    | *   10f1478 (origin/apiTests)
//    | |\
//    | |/
//    |/|
//    * |   e1c681d
//    |\ \
//    | * | 9abfe87 (origin/fixWarnings)
//    |/ /
//    | * 5bc70c8
//    |/
//    *   a0b1d9d
//    |\
//    | * 9d12f9b (origin/addByNameRequests)
//    |/
//    *   53d1e81
//    |\
//    | * add4d90 (origin/server)
//    | * 182ab16
//    | * cb16165
//    | * 571f8b7
//    * | 1ac81ee
//    |\|
//    | * 851eedb (origin/data)
//    | * ea2a863
//    | * aefe80e
//    | * a8ddc2b
//    | * 4dd2485
//    | * 7ad8427
//    |/
//    * d194db3 Initial commit
    @Test
    public void testOneCommonManyWaysToCommon() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "simple-chat", authToken);
        var result = finder.findLastCommonCommits("master", "data");

        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.contains("851eedb006fc3cf68e8e49bed44b6c1ca1349a8f"));
    }

    //    repo tree description
//    *   c0b8160 (HEAD -> master, origin/master, origin/HEAD)
//    |\
//    | *   2aa2fb9 (origin/release/1.0, origin/develop)
//    | |\
//    | | * 4f483b3 (origin/feature/file2
//    | |/
//    | * d32730a
//    |/|
//    | * 56143dd (origin/feature/add_file1)
//    |/
//    * 9db2e09
    @Test
    public void testTheSameCommit() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "testGitKraken", authToken);
        var result = finder.findLastCommonCommits("release/1.0", "develop");

        Assert.assertTrue(result.contains("2aa2fb97588bb26c35d0c7d55d032fda2cd21109"));
        Assert.assertEquals(1, result.size());
    }

    //    repo tree description
//    * 9c297f9 (HEAD -> branchD, origin/branchD)
//    | * acdbed4 (origin/branchC, branchC)
//    | | *   e4c21dc (origin/rebaseCheck, rebaseCheck)
//    | | |\
//    | | | * cf5cd85
//    | | |/
//    | |/|
//    | * |   156cdc1
//    | |\ \
//    | | * | 2f2b8c1 (origin/branchE, branchE)
//    | * | | ce1bc10
//    | | | * c670199
//    | | | * 2c974f0
//    | | | * d999ffd
//    | |_|/
//    |/| |
//    * | |   54c8c3c
//    |\ \ \
//    | * | | 29c9522 (origin/branchB, branchB)
//    | * | | a7af890
//    | |/ /
//    | * | 47c7118
//    * | | a926d9d
//    | |/
//    |/|
//    * | c485a06
//    * | ef01bc7
//    * | a7a3473 (origin/main, origin/HEAD, main)
//    |/
//    | * 28eadc0 (origin/branchWithoutParrent, branchWithoutParrent)
//    | * 294c26b (origin/branchA, branchA)
//    | * 983e14c
//    |/
//    * 0f39676
//    * a7f93b6
    @Test
    public void testTwoCommon() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "DemoRepo", authToken);
        var result = finder.findLastCommonCommits("branchC", "branchD");

        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.containsAll(List.of("47c711847df9c44b9b168fdecf532cf9ec1b5069", "c485a066c9b7e537f9310820add010c447ca4106")));
    }

    @Test
    public void testZeroCommon() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "DemoRepo", authToken);
        var result = finder.findLastCommonCommits("branchC", "branchWithoutParrent");

        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testRebaseCheck() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "DemoRepo", authToken);
        var result = finder.findLastCommonCommits("branchC", "rebaseCheck");

        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.contains("156cdc1f2d404b37444b5cc59ee7e826536eb371"));
    }

    @Test
    public void testZeroCommonBigRepo() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("spring-projects", "spring-framework", authToken);
        var result = finder.findLastCommonCommits("4.1.x", "5.2.x");
        Assert.assertTrue(result.isEmpty());
    }


}