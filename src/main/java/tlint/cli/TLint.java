package tlint.cli;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class TLint {
    public File file = new File();

    static TLint read(String json) {
        TLint lint = new TLint();

        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            lint.file = gson.fromJson(json, File.class);
        } catch (JsonSyntaxException e) {
            //
        }

        return lint;
    }

    public static class File {
        public List<Issue> errors = new ArrayList<>();
    }

    public static class Issue {
        public String source;
        public int line;
        public String message;
    }
}