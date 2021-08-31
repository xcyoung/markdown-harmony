package me.xcyoung.markdown.fraction;

import me.xcyoung.markdown.ResourceTable;
import me.xcyoung.markdown.bean.setting.DownloadSettingVo;
import me.xcyoung.markdown.bean.setting.SettingVo;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.fraction.Fraction;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.ListContainer;
import ohos.app.dispatcher.task.TaskPriority;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class SettingFraction extends Fraction {
    AbilitySlice abilitySlice;

    public SettingFraction(AbilitySlice abilitySlice) {
        super();
        this.abilitySlice = abilitySlice;
    }

    @Override
    protected Component onComponentAttached(LayoutScatter scatter, ComponentContainer container, Intent intent) {
        return scatter.parse(ResourceTable.Layout_fraction_setting, container, false);
    }

    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        ListContainer listContainer = (ListContainer)getFractionAbility().findComponentById(ResourceTable.Id_settingListContainer);
        SettingListProvider settingListProvider = new SettingListProvider(abilitySlice);
        List<SettingVo> list = new ArrayList<>();
        list.add(new DownloadSettingVo());
        settingListProvider.setData(list);
        listContainer.setItemProvider(settingListProvider);
        settingListProvider.setOnEventListener(this::onDownload);
    }

    private void onDownload(String address) {
        getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(() -> {
            try {
                File file = new File(getApplicationContext().getExternalCacheDir(), "abcd.zip");
                if (!getApplicationContext().getExternalCacheDir().exists()) {
                    getApplicationContext().getExternalCacheDir().mkdirs();
                    file.createNewFile();
                } else if (!file.exists()) {
                    file.createNewFile();
                }
                download(address, file.getAbsolutePath());
                getMainTaskDispatcher().asyncDispatch(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                getMainTaskDispatcher().asyncDispatch(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        });
    }

    /**
     * 下载文件到本地
     * @param urlString 被下载的文件地址
     * @param filename 本地文件名
     * @throws Exception 各种异常
     */
    private void download(String urlString, String filename) throws Exception {
        // 构造URL
        URL url = new URL(urlString);
        // 打开连接
        URLConnection con = url.openConnection();
        // 输入流
        InputStream is = con.getInputStream();
        // 1K的数据缓冲
        byte[] bs = new byte[1024];
        // 读取到的数据长度
        int len;
        // 输出的文件流
        OutputStream os = new FileOutputStream(filename);
        // 开始读取
        while ((len = is.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        // 完毕，关闭所有链接
        os.close();
        is.close();
    }
}
