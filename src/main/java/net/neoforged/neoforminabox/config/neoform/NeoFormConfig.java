package net.neoforged.neoforminabox.config.neoform;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import net.neoforged.neoforminabox.utils.MavenCoordinate;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

public record NeoFormConfig(int spec,
                            @SerializedName("version") String minecraftVersion,
                            boolean official,
                            @SerializedName("java_target") String javaVersion,
                            String encoding,
                            Map<String, Object> data,
                            Map<String, List<NeoFormStep>> steps,
                            Map<String, NeoFormFunction> functions,
                            Map<String, List<MavenCoordinate>> libraries) {

    public NeoFormDistConfig getDistConfig(String dist) {
        if (!steps.containsKey(dist)) {
            throw new IllegalArgumentException("This configuration does not include the distribution "
                                               + dist + ". Available: " + steps.keySet());
        }
        return new NeoFormDistConfig(this, dist);
    }

    public static NeoFormConfig from(ZipFile zipFile) throws IOException {
        byte[] configContent;
        var configEntry = zipFile.getEntry("config.json");
        if (configEntry == null || configEntry.isDirectory()) {
            throw new IOException("NeoForm config file config.json not found in " + zipFile.getName());
        }

        try (var in = zipFile.getInputStream(configEntry)) {
            configContent = in.readAllBytes();
        }

        var gson = new GsonBuilder()
                .registerTypeAdapter(NeoFormStep.class, new NeoFormStepDeserializer())
                .registerTypeAdapter(MavenCoordinate.class, MavenCoordinate.TYPE_ADAPTER)
                .create();
        var root = gson.fromJson(new StringReader(new String(configContent, StandardCharsets.UTF_8)), JsonObject.class);

        return gson.fromJson(root, NeoFormConfig.class);
    }

    @Nullable
    public String getDataPathInZip(String name, String dist) {
        var dataSpec = data.get(name);
        if (dataSpec == null) {
            return null;
        }

        if (dataSpec instanceof String stringValue) {
            return stringValue;
        } else if (dataSpec instanceof Map<?, ?> mapValue) {
            return (String) mapValue.get(dist);
        } else {
            throw new UnsupportedOperationException("Unsupported data type " + dataSpec.getClass().getName() + " for data " + name);
        }
    }
}

