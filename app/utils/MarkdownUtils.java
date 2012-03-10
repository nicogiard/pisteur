package utils;

import com.petebevin.markdown.MarkdownProcessor;

public final class MarkdownUtils {

    public static MarkdownUtils instance;

    private MarkdownProcessor markdownProcessor;

    private MarkdownUtils() {
        markdownProcessor = new MarkdownProcessor();
    }

    public static MarkdownUtils get() {
        if (instance == null) {
            instance = new MarkdownUtils();
        }
        return instance;
    }

    public String process(String content){
        return markdownProcessor.markdown(content);
    }
}
