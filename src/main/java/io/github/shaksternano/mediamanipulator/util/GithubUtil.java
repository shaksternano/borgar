package io.github.shaksternano.mediamanipulator.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.*;

public class GithubUtil {

    /**
     * Gets the list of file names in the given directory.
     *
     * @param user       The name of the user that owns the repository.
     * @param repository The name of the repository.
     * @param commitSha  The SHA of the commit to get the list of files from.
     * @param path       The path to the directory to get the list of files from.
     * @return The list of file names in the given directory.
     */
    public static List<String> listFiles(String user, String repository, String commitSha, String... path) {
        return listFiles(user, repository, commitSha, new LinkedList<>(Arrays.asList(path)));
    }

    private static List<String> listFiles(String user, String repository, String commitSha, List<String> path) {
        String url = getRepositoryApiUrl(user, repository) + "/git/trees/" + commitSha;
        JsonElement content = NetworkUtil.httpGet(url);
        Optional<JsonElement> treeElementOptional = JsonUtil.getNestedElement(content, "tree");
        Optional<JsonArray> treeArrayOptional = treeElementOptional.map(treeElement -> {
            if (treeElement.isJsonArray()) {
                return treeElement.getAsJsonArray();
            } else {
                return null;
            }
        });

        if (treeArrayOptional.isPresent()) {
            JsonArray treeArray = treeArrayOptional.orElseThrow();
            if (path.isEmpty()) {
                List<String> files = new ArrayList<>();
                for (JsonElement fileElement : treeArray) {
                    JsonUtil.getNestedElement(fileElement, "path").flatMap(JsonUtil::getString).ifPresent(files::add);
                }
                return files;
            } else {
                String firstPath = path.remove(0);
                for (JsonElement treeElement : treeArray) {
                    Optional<String> pathOptional = JsonUtil.getNestedElement(treeElement, "path").flatMap(JsonUtil::getString);
                    if (Optional.of(firstPath).equals(pathOptional)) {
                        Optional<String> shaOptional = JsonUtil.getNestedElement(treeElement, "sha").flatMap(JsonUtil::getString);
                        if (shaOptional.isPresent()) {
                            return listFiles(user, repository, shaOptional.orElseThrow(), path);
                        }
                    }
                }
            }
        }

        return ImmutableList.of();
    }

    public static Optional<String> getLatestReleaseTagCommitSha(String user, String repository) {
        Optional<String> latestReleaseTagOptional = getLatestReleaseTag(user, repository);
        if (latestReleaseTagOptional.isPresent()) {
            JsonElement tags = NetworkUtil.httpGet("https://api.github.com/repos/twitter/twemoji/tags");
            if (tags.isJsonArray()) {
                JsonArray tagsArray = tags.getAsJsonArray();
                for (JsonElement tagElement : tagsArray) {
                    Optional<String> tag = JsonUtil.getNestedElement(tagElement, "name").flatMap(JsonUtil::getString);
                    if (latestReleaseTagOptional.equals(tag)) {
                        return JsonUtil.getNestedElement(tagElement, "commit", "sha").flatMap(JsonUtil::getString);
                    }
                }
            }
        }

        return Optional.empty();
    }

    private static Optional<String> getLatestReleaseTag(String user, String repository) {
        String url = getRepositoryApiUrl(user, repository) + "/releases/latest";
        JsonElement latestRelease = NetworkUtil.httpGet(url);
        return JsonUtil.getNestedElement(latestRelease, "tag_name").flatMap(JsonUtil::getString);
    }

    private static String getRepositoryApiUrl(String user, String repository) {
        return "https://api.github.com/repos/" + user + "/" + repository;
    }
}
