package ru.sailor;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class GithubLastCommonCommitsFinderTest {

    private final String authToken = "";

    @Test
    public void testTheSameBranch() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "Test_db", authToken);
        Assert.assertTrue(finder.findLastCommonCommits("master", "master").contains("099a79ac0581230cec78cdbdff5b581d6ef9105f"));
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
        Assert.assertTrue(finder.findLastCommonCommits("useQuery", "addTests").contains("6177e1b22fcb68622bb1c7c1b24c72d7459a94e5"));
    }

    @Test
    public void testOneCommonAIsHigher() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "site-statistic", authToken);
        Assert.assertTrue(finder.findLastCommonCommits("addTests", "useQuery").contains("6177e1b22fcb68622bb1c7c1b24c72d7459a94e5"));
    }

    @Test
    public void testOneCommonAIsHigherWithMaster() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "site-statistic", authToken);
        Assert.assertTrue(finder.findLastCommonCommits("master", "useQuery").contains("6177e1b22fcb68622bb1c7c1b24c72d7459a94e5"));
    }

    @Test
    public void testOneCommonAIsHigherBIsAParent() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "site-statistic", authToken);
        Assert.assertTrue(finder.findLastCommonCommits("master", "addTests").contains("031e1da59d2c6d24e9a883f905fcbaaacfdf68e2"));
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
        Assert.assertTrue(finder.findLastCommonCommits("master", "data").contains("851eedb006fc3cf68e8e49bed44b6c1ca1349a8f"));

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
        Assert.assertTrue(finder.findLastCommonCommits("release/1.0", "develop").contains("2aa2fb97588bb26c35d0c7d55d032fda2cd21109"));
    }

//    repo tree description
//    *   156cdc1 (HEAD -> branchC, origin/branchC)
//    |\
//    | * 2f2b8c1 (origin/branchE, branchE)
//    * | ce1bc10
//    | | *   54c8c3c (origin/branchD, branchD)
//    | | |\
//    | | | * 29c9522 (origin/branchB, branchB)
//    | | | * a7af890
//    | |_|/
//    |/| |
//    * | | 47c7118
//    | | * a926d9d
//    | |/
//    | * c485a06
//    | * ef01bc7
//    | * a7a3473 (origin/main, origin/HEAD, main)
//    |/
//    | * 294c26b (origin/branchA, branchA)
//    | * 983e14c
//    |/
//    * 0f39676
//    * a7f93b6
    @Test
    public void testTwoCommon() throws IOException {
        var finder = new GithubLastCommonCommitsFinder("MikhniukR", "DemoRepo", authToken);
        Assert.assertTrue(finder.findLastCommonCommits("branchC", "branchD")
                .containsAll(List.of("47c711847df9c44b9b168fdecf532cf9ec1b5069", "c485a066c9b7e537f9310820add010c447ca4106")));
    }

}