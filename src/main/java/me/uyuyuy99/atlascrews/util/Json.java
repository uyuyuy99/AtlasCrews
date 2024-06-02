package me.uyuyuy99.atlascrews.util;

import com.google.common.base.Supplier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.uyuyuy99.atlascrews.crew.Crew;

import java.io.*;

public final class Json {

    public static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .registerTypeAdapter(Crew.class, new Crew.Serializer())
            .create();

    private Json() { }

    public static <T> T load(final File file, Class<T> type, Supplier<T> dfl) {
        if (!file.exists()) {
            return dfl.get();
        }
        try (FileReader fr = new FileReader(file)) {
            return gson.fromJson(fr, type);
        } catch (FileNotFoundException fnfr) {
            return dfl.get();
        } catch (IOException ioe) {
            throw new IllegalStateException("Loading " + file, ioe);
        }
    }

    public static <T> T load(final File file, Class<T> type) {
        return load(file, type, () -> null);
    }

    public static void save(final File file, Object obj, boolean pretty) {
        try (FileWriter fw = new FileWriter(file)) {
            gson.toJson(obj, fw);
        } catch (IOException ioe) {
            throw new IllegalStateException("Saving " + file, ioe);
        }
    }

    public static void save(final File file, Object obj) {
        save(file, obj, false);
    }

    public static String serialize(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T deserialize(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

}
