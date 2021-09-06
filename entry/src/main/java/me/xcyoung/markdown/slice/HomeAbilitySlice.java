package me.xcyoung.markdown.slice;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;
import me.xcyoung.markdown.ResourceTable;
import me.xcyoung.markdown.bean.NotePo;
import me.xcyoung.markdown.download.DownloadTaskCenter;
import me.xcyoung.markdown.utils.FileUtils;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.*;
import ohos.agp.utils.Color;
import ohos.agp.window.dialog.CommonDialog;
import ohos.agp.window.dialog.ToastDialog;
import ohos.data.DatabaseHelper;
import ohos.data.preferences.Preferences;
import ohos.global.resource.NotExistException;
import ohos.global.resource.WrongTypeException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class HomeAbilitySlice extends AbilitySlice {
    private Preferences notesDB;

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

            createPreferences();
        } catch (IOException | NotExistException | WrongTypeException e) {
            e.printStackTrace();
        }
    }

    private void createPreferences() {
        // getContext(); 数据文件存储路径：/data/data/{PackageName}/{AbilityName}/preferences。
        // getApplicationContext(); 数据文件存储路径：/data/data/{PackageName}/preferences。
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext()); // context入参类型为ohos.app.Context。
        String fileName = "notes_database";  // fileName表示文件名，其取值不能为空，也不能包含路径，默认存储目录可以通过context.getPreferencesDir()获取。
        notesDB = databaseHelper.getPreferences(fileName);
    }

    private void onBoomMenuLocalClick() {
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder().withAction("android.intent.action.GET_CONTENT").build();
        intent.setOperation(operation);
        intent.addFlags(Intent.FLAG_NOT_OHOS_COMPONENT);
        intent.setType("file/*");
        startAbilityForResult(intent, 100);
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
        DownloadTaskCenter.getInstance().createDownloadTask(downloadUrl,
                getApplicationContext().getExternalCacheDir().getAbsolutePath(),
                new DownloadTaskCenter.OnDownloadEventListener() {
                    @Override
                    public void onDownloadSuccess(String downloadUrl, String saveFilePath, String saveFileName) {
                        boolean zip = FileUtils.unZip(saveFilePath, getApplicationContext().getExternalCacheDir().getAbsolutePath(), false);
                        if (zip) {
                            String notesJson = notesDB.getString("Notes", "[]");
                            Gson gson = new Gson();
                            List<NotePo> notePos = gson.fromJson(notesJson, new TypeToken<List<NotePo>>() {
                            }.getType());
                            NotePo newNote = new NotePo(downloadUrl,
                                    new File(getApplicationContext().getExternalCacheDir(), saveFileName.replace(".zip", "")).getAbsolutePath(),
                                    NotePo.NoteAddType.REMOTE,
                                    saveFileName.replace(".zip", "")
                            );
                            notePos.add(newNote);
                            String newNotesJson = gson.toJson(notePos);
                            notesDB.putString("Notes", newNotesJson);
                            notesDB.flush();

                            ToastDialog dialog = new ToastDialog(getContext());
                            dialog.setText("onDownloadSuccess" + saveFileName);
                            dialog.show();
                        } else {
                            ToastDialog dialog = new ToastDialog(getContext());
                            dialog.setText("zip file" + downloadUrl);
                            dialog.show();
                        }
                    }

                    @Override
                    public void onDownloadFailed(String message) {
                        ToastDialog dialog = new ToastDialog(getContext());
                        dialog.setText("onDownloadFailed" + message);
                        dialog.show();
                    }
                }
        );
    }

    @Override
    protected void onAbilityResult(int requestCode, int resultCode, Intent resultData) {
        super.onAbilityResult(requestCode, resultCode, resultData);
        if (requestCode == 100) {

        }
    }
}
