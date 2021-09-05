package me.xcyoung.markdown.slice;

import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;
import me.xcyoung.markdown.ResourceTable;
import me.xcyoung.markdown.download.DownloadTaskCenter;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.*;
import ohos.agp.utils.Color;
import ohos.agp.window.dialog.CommonDialog;
import ohos.agp.window.dialog.ToastDialog;
import ohos.global.resource.NotExistException;
import ohos.global.resource.WrongTypeException;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class HomeAbilitySlice extends AbilitySlice {

    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        setUIContent(ResourceTable.Layout_ability_slice_home);
        try {
            BoomMenuButton bmb = (BoomMenuButton) findComponentById(ResourceTable.Id_boom);
            bmb.setButtonEnum(ButtonEnum.TextOutsideCircle);
            bmb.setPiecePlaceEnum(PiecePlaceEnum.DOT_2_1);
            bmb.setButtonPlaceEnum(ButtonPlaceEnum.SC_2_1);
            bmb.setShadowEffect(true);
            bmb.setRippleEffect(true);
            bmb.setBackgroundEffect(true);

            bmb.addBuilder(new TextOutsideCircleButton.Builder()
                    .normalText("local")
                    .normalColorRes(ohos.global.systemres.ResourceTable.Color_id_color_card_bg)
                    .highlightedColorRes(ohos.global.systemres.ResourceTable.Color_id_color_card_bg)
                    .listener(i -> onBoomMenuLocalClick())
            );
            bmb.addBuilder(new TextOutsideCircleButton.Builder()
                    .normalText("remote")
                    .normalColorRes(ohos.global.systemres.ResourceTable.Color_id_color_card_bg)
                    .highlightedColorRes(ohos.global.systemres.ResourceTable.Color_id_color_card_bg)
                    .listener(i -> onBoomMenuRemoteClick())
            );

            bmb.setDotRadius(5);
            bmb.setButtonHorizontalMargin(AttrHelper.vp2px(160, getContext()));

            Color bmbNormalColor = new Color(getResourceManager().getElement(
                    ohos.global.systemres.ResourceTable.Color_id_color_floating_button_bg_normal).getColor());
            Color bmbHighlightColor = new Color(getResourceManager().getElement(
                    ohos.global.systemres.ResourceTable.Color_id_color_floating_button_bg_pressed).getColor());
            bmb.setNormalColor(bmbNormalColor);
            bmb.setHighlightedColor(bmbHighlightColor);
            bmb.buildButton();
        } catch (IOException | NotExistException | WrongTypeException e) {
            e.printStackTrace();
        }
    }

    private void onBoomMenuLocalClick() {

    }

    private void onBoomMenuRemoteClick() {
        CommonDialog dialog = new CommonDialog(getContext());
        Component container = LayoutScatter.getInstance(getContext()).parse(ResourceTable.Layout_dialog_add_markdown,
                null, false);
        dialog.setContentCustomComponent(container);
        dialog.setSize(AttrHelper.vp2px((float) (getResourceManager().getDeviceCapability().width * 0.75),
                getContext()), AttrHelper.vp2px((float) (getResourceManager().getDeviceCapability().height * 0.50), getContext()));

        Button confirmBtn = (Button) container.findComponentById(ResourceTable.Id_confirmBtn);
        TextField textField = (TextField) container.findComponentById(ResourceTable.Id_textField);
        confirmBtn.setClickedListener(component -> {
            String url = textField.getText();
            remoteDownload(url);
            dialog.destroy();
        });
        dialog.show();
    }

    private void remoteDownload(String downloadUrl) {
        String saveName = UUID.randomUUID().toString();

        DownloadTaskCenter.getInstance().createDownloadTask(downloadUrl,
                new File(getApplicationContext().getExternalCacheDir(), saveName + ".zip").getAbsolutePath(),
                new DownloadTaskCenter.OnDownloadEventListener() {
                    @Override
                    public void onDownloadSuccess(String downloadUrl, String saveFilePath) {
                        ToastDialog dialog = new ToastDialog(getContext());
                        dialog.setText("onDownloadSuccess");
                        dialog.show();
                    }

                    @Override
                    public void onDownloadFailed(String message) {
                        ToastDialog dialog = new ToastDialog(getContext());
                        dialog.setText("onDownloadFailed");
                        dialog.show();
                    }
                }
        );
    }
}
