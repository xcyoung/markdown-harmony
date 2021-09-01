package me.xcyoung.markdown.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadTaskCenter {
    static DownloadTaskCenter singleton;
    private final ExecutorService pool;
    private Map<String, DownloadTaskField> queueMap = new ConcurrentHashMap<>();

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
            return;
        }

        try {
            File file = new File(taskField.getSaveFilePath());
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } else if (!file.exists()) {
                file.createNewFile();
            }
            download(taskField.getDownloadUrl(), taskField.getSaveFilePath());
            taskField.getListener().onDownloadSuccess(taskField.getSaveFilePath());
        } catch (Exception e) {
            e.printStackTrace();
            taskField.getListener().onDownloadFailed(e.getMessage());
        }
        queueMap.remove(taskField.getDownloadUrl());
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
        private final OnDownloadEventListener listener;

        public DownloadTaskField(String downloadUrl, String saveFilePath, OnDownloadEventListener listener) {
            this.downloadUrl = downloadUrl;
            this.saveFilePath = saveFilePath;
            this.listener = listener;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public String getSaveFilePath() {
            return saveFilePath;
        }

        public OnDownloadEventListener getListener() {
            return listener;
        }
    }

    public interface OnDownloadEventListener {
        void onDownloadSuccess(String saveFilePath);

        void onDownloadFailed(String message);
    }
}