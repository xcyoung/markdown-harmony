package me.xcyoung.markdown.download;

import me.xcyoung.markdown.utils.FileUtils;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    public boolean createDownloadTask(String downloadUrl, String saveFilePath, OnDownloadEventListener listener) {
        if (queueMap.containsKey(downloadUrl)) {
            return false;
        }

        DownloadTaskField taskField = new DownloadTaskField(downloadUrl, saveFilePath, listener);
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
            boolean createOrExistFile = FileUtils.createOrExistsFile(taskField.getSaveFilePath());
            if (!createOrExistFile) {
                throw new IOException("createOrExistsFile error");
            }
            download(taskField.getDownloadUrl(), taskField.getSaveFilePath());
            mainEventHandler.postSyncTask(() -> {
                OnDownloadEventListener listener = taskField.getListener();
                if (listener != null) listener.onDownloadSuccess(
                        taskField.getDownloadUrl(),
                        taskField.getSaveFilePath());
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

    /**
     * 下载文件到本地
     *
     * @param urlString    被下载的文件地址
     * @param saveFilePath 本地文件名
     * @throws Exception 各种异常
     */
    private void download(String urlString, String saveFilePath) throws Exception {
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
        OutputStream os = new FileOutputStream(saveFilePath);
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
        private final String saveFilePath;
        private final WeakReference<OnDownloadEventListener> listener;

        public DownloadTaskField(String downloadUrl, String saveFilePath, OnDownloadEventListener listener) {
            this.downloadUrl = downloadUrl;
            this.saveFilePath = saveFilePath;
            this.listener = new WeakReference<>(listener);
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public String getSaveFilePath() {
            return saveFilePath;
        }

        public OnDownloadEventListener getListener() {
            return listener.get();
        }
    }

    public interface OnDownloadEventListener {
        void onDownloadSuccess(String downloadUrl, String saveFilePath);

        void onDownloadFailed(String message);
    }
}
