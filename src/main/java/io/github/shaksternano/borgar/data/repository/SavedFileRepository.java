package io.github.shaksternano.borgar.data.repository;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.Map;
import java.util.Optional;

public class SavedFileRepository {

    private static final DB db = DBMaker.fileDB("database.mapdb")
        .transactionEnable()
        .closeOnJvmShutdown()
        .make();

    /**
     * Maps a URL to its alias URL.
     */
    private static final Map<String, String> urlAliases = db.hashMap("url_aliases")
        .keySerializer(Serializer.STRING)
        .valueSerializer(Serializer.STRING)
        .createOrOpen();

    public static void addAlias(String url, String aliasUrl) {
        urlAliases.put(url, aliasUrl);
        db.commit();
    }

    public static Optional<String> getAlias(String url) {
        return Optional.ofNullable(urlAliases.get(url));
    }
}
