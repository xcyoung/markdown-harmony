package me.xcyoung.markdown.parser;

import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.attributes.AttributesExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.*;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.html.MutableAttributes;
import com.vladsch.flexmark.util.misc.Extension;
import com.vladsch.flexmark.util.sequence.Escaping;
import ohos.app.Context;
import ohos.global.resource.RawFileEntry;
import ohos.global.resource.Resource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 参考自：https://github.com/Shouheng88/EasyMark/blob/master/easymark/src/main/java/me/shouheng/easymark/viewer/parser/MarkdownParser.java
 */
public class MarkdownParser {
    private static final Pattern mathJaxPattern = Pattern.compile(ParserConstants.MATH_JAX_REGEX_EXPRESSION);

    private final List<Extension> extensions;

    private final DataHolder options;

    private final WeakReference<Context> contextWeakReference;

    public MarkdownParser(Context context) {
        extensions = new LinkedList<>();
        extensions.addAll(Arrays.asList(
                TablesExtension.create(),
                TaskListExtension.create(),
                AbbreviationExtension.create(),
                AutolinkExtension.create(),
                StrikethroughSubscriptExtension.create(),
                SuperscriptExtension.create(),
                FootnoteExtension.create(),
                AttributesExtension.create()));

        options = new MutableDataSet()
                .set(FootnoteExtension.FOOTNOTE_REF_PREFIX, "[")
                .set(FootnoteExtension.FOOTNOTE_REF_SUFFIX, "]")
                .set(HtmlRenderer.FENCED_CODE_LANGUAGE_CLASS_PREFIX, "")
                .set(HtmlRenderer.FENCED_CODE_NO_LANGUAGE_CLASS, "nohighlight");

        contextWeakReference = new WeakReference<>(context);
    }

    public String parser(String markdown, String theme) throws IOException {
        Parser parser = Parser.builder(options)
                .extensions(extensions)
                .build();

        HtmlRenderer renderer = HtmlRenderer.builder(options)
                .attributeProviderFactory(new IndependentAttributeProviderFactory() {
                    @Override
                    public @NotNull AttributeProvider apply(@NotNull LinkResolverContext linkResolverContext) {
                        return new CustomAttributeProvider();
                    }
                })
                .nodeRendererFactory(new NodeRendererFactoryImpl())
                .extensions(extensions)
                .build();

        String noteHtml = renderer.render(parser.parse(markdown));
        return getHtml(noteHtml, theme);
    }

    private String loadResource(String path) throws IOException {
        RawFileEntry entry = contextWeakReference.get().getResourceManager()
                .getRawFileEntry("resources/rawfile/" + path);
        Resource resource = entry.openRawFile();
        byte[] buffer = new byte[resource.available()];
        int a = resource.read(buffer, 0, resource.available());
        return new String(buffer);
    }

    private String getHtml(String noteHtml, String theme) throws IOException {
        String temple = loadResource("markdown/temple.html");
        String themeCss = loadResource(String.format("markdown/style/%s/theme.css", theme));
        String highlightCss = loadResource(String.format("markdown/style/%s/hightlight.min.css", theme));
        String highlightJs = loadResource("markdown/style/highlight.min.js");

        temple = temple.replace("$theme_css", themeCss);
        temple = temple.replace("$highlight_css", highlightCss);
        temple = temple.replace("$highlight_js", highlightJs);
        temple = temple.replace("$content", noteHtml);

        return temple;
    }

    /**
     * Custom attribute provider
     * <p>
     * Extension point for adding/changing attributes on the primary HTML tag for a node.
     */
    public static class CustomAttributeProvider implements AttributeProvider {
        @Override
        public void setAttributes(@NotNull Node node, @NotNull AttributablePart attributablePart, @NotNull MutableAttributes mutableAttributes) {

        }
    }

    /**
     * Custom the node renderer
     * <p>
     * Factory for instantiating new node renderers when rendering is done.
     */
    public static class NodeRendererFactoryImpl implements NodeRendererFactory {
        private NodeRenderingHandler<Image> getImageRenderingHandler() {
            return new NodeRenderingHandler<>(Image.class, new NodeRenderingHandler.CustomNodeRenderer<Image>() {
                @Override
                public void render(@NotNull Image node, @NotNull NodeRendererContext context, @NotNull HtmlWriter html) {
                    if (!context.isDoNotRenderLinks()) {
                        String altText = new TextCollectingVisitor().collectAndGetText(node);

                        ResolvedLink resolvedLink = context.resolveLink(LinkType.IMAGE, node.getUrl().unescape(), null);
                        String url = resolvedLink.getUrl();

                        if (!node.getUrlContent().isEmpty()) {
                            // reverse URL encoding of =, &
                            String content = Escaping.percentEncodeUrl(node.getUrlContent())
                                    .replace("+", "%2B")
                                    .replace("%3D", "=")
                                    .replace("%26", "&amp;");
                            url += content;
                        }

                        final int index = url.indexOf('@');

                        if (index >= 0) {
                            String[] dimensions = url.substring(index + 1).split("\\|");
                            url = url.substring(0, index);
                            if (dimensions.length == 2) {
                                String width = dimensions[0] == null || dimensions[0].equals("") ? "auto" : dimensions[0];
                                String height = dimensions[1] == null || dimensions[1].equals("") ? "auto" : dimensions[1];
                                html.attr("style", "width: " + width + "; height: " + height);
                            }
                        }

                        html.attr("src", url);
                        html.attr("alt", altText);

                        if (node.getTitle().isNotNull()) {
                            html.attr("title", node.getTitle().unescape());
                        }

                        html.srcPos(node.getChars()).withAttr(resolvedLink).tagVoid("img");
                    }
                }
            });
        }

        @Override
        public @NotNull NodeRenderer apply(@NotNull DataHolder options) {
            return new NodeRenderer() {
                @Override
                public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
                    HashSet<NodeRenderingHandler<?>> set = new HashSet<>();
                    set.add(getImageRenderingHandler());
//                    set.add(getMathJaxRenderingHandler());
                    return set;
                }
            };
        }
    }

    public static class MarkdownParserFactory {
        public static MarkdownParser create(Context context) {
            MarkdownParser parser = new MarkdownParser(context);
            return parser;
        }
    }
}
