# LastCommonCommitsFinder

## How to 
### Clone
```
git clone https://github.com/MikhniukR/LastCommonCommitsFinder.git
```
### Make .jar
```
cd LastCommonCommitsFinder/
mvn package -DskipTests
or
mvn clean install -DskipTests 
```
### Run tests
```
cd LastCommonCommitsFinder/
mvn test
```
[Go to test description](#tests)

## Description
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
Достижимостью коммита из ветки или другого коммита будем называть возможность достигнуть его двигаясь "в прошлое"
по истории изменений.

Под последними общими коммитами мы будем понимать такие коммиты, которые достижимы из обеих веток и при этом не достижимы
ни из каких других коммитов достижимых из обеих веток.

Результатом работы метода *findLastCommonCommits* должна быть коллекция *SHA* последних общих коммитов для двух заданных
веток. В общем случае таких последних коммитов может быть более одного.

Предполагается, что претендент будет использовать *GitHub REST* или *GraphQL API* через ту или иную библиотеку 
реализующую HTTP клиента самостоятельно, не используя специальных реализаций GitHub *REST/GraphQL API* клиента.

Реализация кеширования приветствуется.

От претендентов ожидается production level код, с тестами и адекватной обработкой ошибок. Результаты принимаются в виде
ссылок на репозитории на том или ином VCS hosting сервисе.

## Solution


Для работы с гитхабом используется *REST API*, переход на *GraphQL API* в теории должен заметно ускорить работу 
библиотеки, уменьшив нагрузку на сеть и уменьшив объем данных для парсинга.
Через REST при любом из запросов передается много лишних данных о коммите. 
К сожалению на данную фичу у меня не хватило времени.

Со стороны Java для запросов используется *apache httpclient + jackson*.

Перейдем к алгоритму поиска. Очевидно что дерево коммитов является графом, где коммиты являются вершинами, а отношение 
родитель ребрами. Весь граф заранее не известен и при отсутствии информации о вершине, а наличии лишь ссылки на неё
(в виде sha коммита) будем запрашивать максимально возможное количество коммитов для одного запроса, после получения 
коммитов они кешируются в рамках одного инстанса GithubLastCommonCommitsFinder.

Идея алгоритма поиска: рассматривать по одному коммиты в хронологическом порядке от самого нового к более старым.
Для этого будем использовать приоритетную очередь. Для старта добавим в неё стартовые коммиты(последние коммиты каждого
из branch-ей), после чего на каждой итерации будем добавлять в очередь потомков данной вершины. При этом будем помечать
для какой ветки(branch-a) данный коммит является достижимым. Если в процессе поиска мы оказались в вершине достижимой из
обеих веток (branch-ей), то она попадает в ответ и её потомки не попадают в очередь для поиска. Поскольку возможно
попасть в одну вершину несколькими путями, то из каждой из достижимых вершин нужно продолжить поиск, но с меткой о том 
что все достижимые из неё вершины достижимы, что бы не добавить в результат "лишние" вершины.

## Tests
В большинстве тестов понадобится токен для github API, поскольку лимит анонимных запросов довольно мал. 
Сейчас токен в тестах захардкожен. Возможно большая часть тестов не будет проходить из-за ограничения на запрос
так же и для не анонимных запросов, для исправления стоит обновить токен. 

Новый токен можно получить на данной [странице](https://github.com/settings/tokens). 
Заменить в тестах GithubLastCommonCommitsFinderTest и GithubClientTest.