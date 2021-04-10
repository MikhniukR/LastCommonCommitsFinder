package ru.sailor.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubCommit {

    String sha;
    List<GithubCommit> parents;
    LocalDateTime timestamp;

    public GithubCommit() {
        parents = Collections.emptyList();
    }

    //todo refactor, it's not looks beautiful
    @JsonProperty("commit")
    private void unpackNested(Map<String, Object> commit) {
        Map<String, Object> author = (Map<String, Object>) commit.get("author");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        timestamp = LocalDateTime.parse((CharSequence) author.get("date"), formatter);
    }

}
