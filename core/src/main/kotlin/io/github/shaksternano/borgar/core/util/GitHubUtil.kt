package io.github.shaksternano.borgar.core.util

import io.github.shaksternano.borgar.core.io.httpGet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Gets the list of file names in the given directory.
 *
 * @param user       The name of the user that owns the repository.
 * @param repository The name of the repository.
 * @param commitSha  The SHA of the commit to get the list of files from.
 * @param path       The path to the directory to get the list of files from.
 * @return The list of file names in the given directory.
 */
suspend fun listGitHubFiles(user: String, repository: String, commitSha: String, vararg path: String): List<String> =
    listGitHubFiles(user, repository, commitSha, path.asList())

private tailrec suspend fun listGitHubFiles(
    user: String,
    repository: String,
    commitSha: String,
    path: List<String>
): List<String> {
    val url = getRepositoryApiUrl(user, repository) + "/git/trees/" + commitSha
    val tree = httpGet<GitHubTreeResponse>(url).tree
    return if (path.isEmpty()) {
        tree.map {
            it.path
        }
    } else {
        val firstPath = path.first()
        val treeDirectory = tree.first {
            it.path == firstPath
        }
        listGitHubFiles(user, repository, treeDirectory.sha, path.drop(1))
    }
}

suspend fun getGitHubLatestReleaseTagCommitSha(user: String, repository: String): String {
    val latestReleaseTag = getLatestReleaseTag(user, repository)
    val tags = httpGet<List<GitHubTag>>("https://api.github.com/repos/twitter/twemoji/tags")
    return tags.first {
        it.name == latestReleaseTag
    }.commit.sha
}

private suspend fun getLatestReleaseTag(user: String, repository: String): String {
    val url = getRepositoryApiUrl(user, repository) + "/releases/latest"
    val latestRelease = httpGet<GitHubReleaseResponse>(url)
    return latestRelease.tagName
}

private fun getRepositoryApiUrl(user: String, repository: String): String =
    "https://api.github.com/repos/$user/$repository"

@Serializable
private data class GitHubReleaseResponse(
    @SerialName("tag_name")
    val tagName: String,
)

@Serializable
private data class GitHubTag(
    val name: String,
    val commit: GitHubCommit,
)

@Serializable
private data class GitHubCommit(
    val sha: String,
)

@Serializable
private data class GitHubTreeResponse(
    val tree: List<GitHubTreeElement>,
)

@Serializable
private data class GitHubTreeElement(
    val path: String,
    val sha: String,
)
