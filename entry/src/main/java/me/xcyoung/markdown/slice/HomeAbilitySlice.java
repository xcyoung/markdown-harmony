package me.xcyoung.markdown.slice;

import me.xcyoung.markdown.ResourceTable;
import me.xcyoung.markdown.parser.MarkdownParser;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.StackLayout;
import ohos.agp.components.Text;
import ohos.agp.components.webengine.WebView;
import ohos.global.configuration.Configuration;
import ohos.global.resource.RawFileEntry;
import ohos.global.resource.Resource;

public class HomeAbilitySlice extends AbilitySlice {
    private Text title;
    private Button backBtn;

    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_slice_home);

        Text title = (Text) getAbility().findComponentById(ResourceTable.Id_title);
        Button backBtn = (Button) getAbility().findComponentById(ResourceTable.Id_backBtn);

        title.setText("线程池的实现原理.md");
        StackLayout stackLayout = (StackLayout) getAbility().findComponentById(ResourceTable.Id_markDownContainer);
        WebView webView = new WebView(getContext());
        webView.setWidth(ComponentContainer.LayoutConfig.MATCH_PARENT);
        webView.setHeight(ComponentContainer.LayoutConfig.MATCH_PARENT);
        webView.getWebConfig().setJavaScriptPermit(true);  // 如果网页需要使用JavaScript，增加此行；如何使用JavaScript下文有详细介绍
        stackLayout.addComponent(webView);

        RawFileEntry entry = getApplicationContext().getResourceManager().getRawFileEntry("resources/rawfile/线程池的实现原理.md");
        try {
            Resource resource = entry.openRawFile();
            byte[] buffer = new byte[resource.available()];
            int a = resource.read(buffer, 0, resource.available());

            String theme = getApplicationContext().getResourceManager().getConfiguration().getSystemColorMode()
                    == Configuration.LIGHT_MODE ? "light" : "dark";
            String html = MarkdownParser.MarkdownParserFactory.create(getApplicationContext()).parser(new String(buffer),
                    theme);

            webView.getWebConfig().setJavaScriptPermit(true);

            webView.load(html, "text/html", "utf-8", null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
