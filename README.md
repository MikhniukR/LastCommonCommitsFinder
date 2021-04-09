# LastCommonCommitsFinder

Задание для отбора на летнюю стажировку в JetBrains.


Написать библиотеку для поиска последних коммитов общих для двух веток реализующую следующие интерфейсы:

```
public interface LastCommonCommitsFinder {

    /**
     * Finds SHAs of last commits that are reachable from both
     * branchA and branchB
     *
     * @param branchA   branch name (e.g. "main")
     * @param branchB   branch name (e.g. "dev")
     * @return  a collection of SHAs of last common commits
     * @throws IOException  if any error occurs
     */
    Collection<String> findLastCommonCommits(String branchA, String branchB) throws IOException;

}

public interface LastCommonCommitsFinderFactory {

    /**
     * Creates an instance of LastCommonCommitsFinder for a particular GitHub.com repository.
     * This method must not check connectivity.
     *
     * @param owner repository owner
     * @param repo  repository name
     * @param token personal access token or null for anonymous access
     * @return an instance of LastCommonCommitsFinder
     */
    LastCommonCommitsFinder create(String owner, String repo, String token);

}
```
Достижимостью коммита из ветки или другого коммита будем называть возможность достигнуть его двигаясь "в прошлое" по истории изменений.

Под последними общими коммитами мы будем понимать такие коммиты, которые достижимы из обеих веток и при этом не достижимы ни из каких других коммитов достижимых из обеих веток.

Результатом работы метода findLastCommonCommits должна быть коллекция SHA последних общих коммитов для двух заданных веток. В общем случае таких последних коммитов может быть более одного.

Предполагается, что претендент будет использовать GitHub REST или GraphQL API через ту или иную библиотеку рализующую HTTP клиента самостоятельно, не используя специальных реализаций GitHub REST/GraphQL API клиента.

Реализация кеширования приветствуется.

От претендентов ожидается production level код, с тестами и адекватной обработкой ошибок. Результаты принимаются в виде ссылок на репозитории на том или ином VCS hosting сервисе.
