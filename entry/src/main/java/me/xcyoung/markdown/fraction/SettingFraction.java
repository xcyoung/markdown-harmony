package me.xcyoung.markdown.fraction;

import me.xcyoung.markdown.ResourceTable;
import me.xcyoung.markdown.bean.setting.DownloadSettingVo;
import me.xcyoung.markdown.bean.setting.SettingVo;
import me.xcyoung.markdown.download.DownloadTaskCenter;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.fraction.Fraction;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.ListContainer;
import ohos.agp.window.dialog.ToastDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        ListContainer listContainer = (ListContainer) getFractionAbility().findComponentById(ResourceTable.Id_settingListContainer);
        SettingListProvider settingListProvider = new SettingListProvider(abilitySlice);
        List<SettingVo> list = new ArrayList<>();
        list.add(new DownloadSettingVo());
        settingListProvider.setData(list);
        listContainer.setItemProvider(settingListProvider);
        settingListProvider.setOnEventListener(this::onDownload);
    }

    private void onDownload(String address) {
        String fileName = UUID.randomUUID().toString();
        boolean result = DownloadTaskCenter.getInstance()
                .createDownloadTask(address, new File(getApplicationContext().getExternalCacheDir(), fileName + ".zip")
                        .getAbsolutePath(), new DownloadTaskCenter.OnDownloadEventListener() {
                    @Override
                    public void onDownloadSuccess(String saveFilePath) {
                        getMainTaskDispatcher().asyncDispatch(() -> {
                            ToastDialog toastDialog = new ToastDialog(SettingFraction.this);
                            toastDialog.setText("下载成功：" + saveFilePath);
                            toastDialog.show();
                        });
                    }

                    @Override
                    public void onDownloadFailed(String message) {
                        getMainTaskDispatcher().asyncDispatch(() -> {
                            ToastDialog toastDialog = new ToastDialog(SettingFraction.this);
                            toastDialog.setText("下载失败：" + message);
                            toastDialog.show();
                        });
                    }
                });

        ToastDialog toastDialog = new ToastDialog(SettingFraction.this);
        toastDialog.setText(result + "");
        toastDialog.show();
    }
}
