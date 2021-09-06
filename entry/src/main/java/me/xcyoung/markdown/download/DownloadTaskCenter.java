package me.xcyoung.markdown.download;

import me.xcyoung.markdown.utils.FileUtils;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadTaskCenter {
    static DownloadTaskCenter singleton;
    private final ExecutorService pool;
    private final EventHandler mainEventHandler = new EventHandler(EventRunner.getMainEventRunner());
    private final Map<String, DownloadTaskField> queueMap = new ConcurrentHashMap<>();

    private DownloadTaskCenter() {
        pool = Executors.newFixedThreadPool(3);
    }

    public static DownloadTaskCenter getInstance() {
        if (singleton == null) {
            synchronized (DownloadTaskCenter.class) {
                if (singleton == null) {
                    singleton = new DownloadTaskCenter();
                }
            }
        }
        return singleton;
    }

    public boolean createDownloadTask(String downloadUrl, String saveDirPath, OnDownloadEventListener listener) {
        if (queueMap.containsKey(downloadUrl)) {
            return false;
        }

        DownloadTaskField taskField = new DownloadTaskField(downloadUrl, saveDirPath, listener);
        queueMap.put(downloadUrl, taskField);
        pool.submit(() -> {
            downloadTask(taskField.getDownloadUrl());
        });

        return true;
    }

    private void downloadTask(String downloadUrl) {
        DownloadTaskField taskField = queueMap.get(downloadUrl);
        if (taskField == null) {
            remove(downloadUrl);
            return;
        }

        try {
            download(taskField);
            mainEventHandler.postSyncTask(() -> {
                OnDownloadEventListener listener = taskField.getListener();
                if (listener != null) listener.onDownloadSuccess(
                        taskField.getDownloadUrl(),
                        taskField.getSaveFilePath(),
                        taskField.getSaveFileName());
            });
        } catch (Exception e) {
            e.printStackTrace();
            mainEventHandler.postSyncTask(() -> {
                OnDownloadEventListener listener = taskField.getListener();
                if (listener != null) listener.onDownloadFailed(e.getMessage());
            });
        } finally {
            remove(taskField.getDownloadUrl());
        }
    }

    private void remove(String downloadUrl) {
        queueMap.remove(downloadUrl);
    }

    private void download(DownloadTaskField taskField) throws Exception {
        // 构造URL
        URL url = new URL(taskField.getDownloadUrl());
        // 打开连接
        URLConnection con = url.openConnection();
        String disposition = con.getHeaderField("Content-Disposition");
        String dispositionFileName = disposition.replaceFirst("(?i)^.*filename=\"?([^\"]+)\"?.*$", "$1");
        File saveFile = new File(taskField.getSaveDirPath(), dispositionFileName);
        boolean createOrExistFile = FileUtils.createOrExistsFile(saveFile.getAbsolutePath());
        if (!createOrExistFile) {
            throw new IOException("createOrExistsFile error");
        }
        taskField.setSaveFileName(dispositionFileName);
        // 输入流
        InputStream is = con.getInputStream();
        // 1K的数据缓冲
        byte[] bs = new byte[1024];
        // 读取到的数据长度
        int len;
        // 输出的文件流
        OutputStream os = new FileOutputStream(saveFile);
        // 开始读取
        while ((len = is.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        // 完毕，关闭所有链接
        os.close();
        is.close();
    }

    static class DownloadTaskField {
        private final String downloadUrl;
        private final String saveDirPath;
        private String saveFileName;
        private final WeakReference<OnDownloadEventListener> listener;

        public DownloadTaskField(String downloadUrl, String saveDirPath, OnDownloadEventListener listener) {
            this.downloadUrl = downloadUrl;
            this.saveDirPath = saveDirPath;
            this.listener = new WeakReference<>(listener);
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public String getSaveDirPath() {
            return saveDirPath;
        }

        public String getSaveFileName() {
            return saveFileName;
        }

        public void setSaveFileName(String saveFileName) {
            this.saveFileName = saveFileName;
        }

        public String getSaveFilePath() {
            return new File(saveDirPath, saveFileName).getAbsolutePath();
        }

        public OnDownloadEventListener getListener() {
            return listener.get();
        }
    }

    public interface OnDownloadEventListener {
        void onDownloadSuccess(String downloadUrl, String saveFilePath, String saveFileName);

        void onDownloadFailed(String message);
    }
}
