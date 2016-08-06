package com.panxiong.down.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.panxiong.down.callback.ProgressCallback;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/1.
 * <p/>
 * 断点续传工具对象
 */
public class DownUtil {
    public static final String LIST_NAME = "DOWN_TASK_LIST";
    public static final Map<String, TaskEntity> taskMap = new HashMap<>();  // 网络变化时给外部调用

    /**
     * 获取正在下载的任务列表
     */
    public static List<TaskEntity> getDownTaskList(Context context) {
        String taskListString = ShaPreUtil.get(context, LIST_NAME);
        if (!StringUtil.noNull(taskListString)) return null;
        String[] taskArray = taskListString.replaceAll(" ", "").split("\n");
        List<TaskEntity> taskEntityList = new ArrayList<>();
        for (String task : taskArray) {
            if (StringUtil.noNull(task) && task.contains(",")) {
                String[] taskString = task.split(",");
                taskEntityList.add(
                        new TaskEntity(context, taskString[0], taskString[1], Long.parseLong(taskString[2]), Long.parseLong(taskString[3])));
            }
        }
        return taskEntityList;
    }

    /**
     * 保存下载任务列表
     */
    public static boolean saveDownTaskList(Context context, List<TaskEntity> taskEntityList) {
        if (taskEntityList == null) return false;
        StringBuilder stringBuilder = new StringBuilder("");
        for (TaskEntity task : taskEntityList) {
            stringBuilder
                    .append(task.downUrl).append(",")
                    .append(task.savePath).append(",")
                    .append(task.fileSize).append(",")
                    .append(task.downSize).append("\n");
        }
        return ShaPreUtil.save(context, LIST_NAME, stringBuilder.toString().replaceAll(" ", ""));
    }

    /**
     * 查找指定任务是否已经存在 （null 不存在）
     */
    public static TaskEntity finlDownTask(Context context, TaskEntity task) {
        List<TaskEntity> taskList = getDownTaskList(context);
        if (taskList == null) return null;
        for (TaskEntity t : taskList) {
            if (t.downUrl.trim().equals(task.downUrl.trim()) && t.savePath.trim().equals(task.savePath.trim())) {
                return t;
            }
        }
        return null;
    }

    /**
     * 添加一个下载任务
     */
    public static boolean addDownTask(Context context, TaskEntity task) {
        if (finlDownTask(context, task) != null) return false;
        List<TaskEntity> taskList = getDownTaskList(context);
        if (taskList == null) {
            taskList = new ArrayList<>();
        }
        taskList.add(task);
        taskMap.put(task.downUrl + task.savePath, task);
        return saveDownTaskList(context, taskList);
    }

    /**
     * 更新一个任务
     */
    public static boolean updateDownTask(Context context, TaskEntity task) {
        TaskEntity taskEntity = finlDownTask(context, task);
        if (taskEntity == null) return false;
        if (!removeDownTask(context, taskEntity)) return false;
        taskMap.remove(task.downUrl + task.savePath);
        taskMap.put(task.downUrl + task.savePath, task);
        return addDownTask(context, task);
    }

    /**
     * 删除指定任务
     */
    public static boolean removeDownTask(Context context, TaskEntity task) {
        if (task == null) return false;
        List<TaskEntity> taskList = getDownTaskList(context);
        Iterator<TaskEntity> iterator = taskList.iterator();
        while (iterator.hasNext()) {
            TaskEntity integer = iterator.next();
            if (integer.downUrl.trim().equals(task.downUrl.trim()) && integer.savePath.trim().equals(task.savePath.trim())) {
                iterator.remove();  // 删除掉指定任务
            }
        }
        taskMap.remove(task.downUrl + task.savePath);
        return saveDownTaskList(context, taskList);
    }

    /*下载任务实体 下载地址与保存地址联合可以确定该下载任务的唯一性*/
    public static class TaskEntity extends Thread {
        private Context context;
        public volatile String downUrl;
        public volatile String savePath;
        public volatile long fileSize = 0L; // 文件大小
        public volatile long downSize = 0L; // 下载进度
        public volatile ProgressCallback progressCallback = null;

        private volatile RandomAccessFile currentPart = null;
        private volatile boolean canDown = true;
        private Handler handler = new Handler(Looper.getMainLooper());

        public TaskEntity(Context context, String downUrl, String savePath, ProgressCallback progressCallback) {
            this.context = context;
            this.downUrl = downUrl;
            this.savePath = savePath;
            this.fileSize = 0L;
            this.downSize = 0L;
            this.progressCallback = progressCallback;
        }

        public TaskEntity(Context context, String downUrl, String savePath, long fileSize, long downSize) {
            this.context = context;
            this.downUrl = downUrl;
            this.savePath = savePath;
            this.fileSize = fileSize;
            this.downSize = downSize;
        }

        @Override
        public void run() {
            try {
                currentPart = new RandomAccessFile(savePath, "rw");
                TaskEntity taskEntity = finlDownTask(context, this);
                if (taskEntity == null) {
                    addDownTask(context, this);
                    initDown();
                } else {
                    // task不空说明之前存在相同下载任务
                    this.fileSize = taskEntity.fileSize;
                    this.downSize = taskEntity.downSize;
                    if (downSize > 0) currentPart.seek(downSize); // 跳过指定大小
                    download();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private HttpURLConnection getConnection(String u) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(u).openConnection();
                conn.setConnectTimeout(30 * 1000);
                conn.setReadTimeout(60 * 1000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept-Language", "zh_CN");    // 语言
                conn.setRequestProperty("Charset", "UTF-8");                // 字符
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Accept", "image/gif, iamge/jpeg, image/pjpeg, " +  // 接受内容
                        "application/x-shockwave-flash, application/xaml+xml, " +
                        "application/vnd.ms-xpsdocument, application/x-ms-xbap, " +
                        "application/x-ms-application, application/vnd.ms-excel, " +
                        "application/vnd.ms-powerpoint, application/msword, */*"
                );
                return conn;
            } catch (Exception e) {
                throw new RuntimeException("GET HttpURLConnection ERROR !");
            }
        }

        private void initDown() {
            try {
                HttpURLConnection conn = getConnection(downUrl);
                fileSize = conn.getContentLength(); // 得到要下载文件的大小
                conn.disconnect();
                if (fileSize <= 0) throw new RuntimeException("Failed to get the file size!");
                // 调用下载方法
                download();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void download() {
            try {
                HttpURLConnection conn = getConnection(downUrl);
                conn.setRequestProperty("Range", "bytes=" + downSize + "-" + fileSize); // 读取范围
                InputStream inputStream = conn.getInputStream();
                byte[] buffer = new byte[1024]; // 缓存设置大点可以减少硬盘的读写与回调次数次数
                int hasRead = 0;
                while (canDown && (hasRead = inputStream.read(buffer)) != -1) {
                    currentPart.write(buffer, 0, hasRead);
                    downSize += hasRead;

                    if (progressCallback != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressCallback.onDownProgress(
                                        downSize, fileSize, downSize >= fileSize, (int) ((double) downSize / (double) fileSize * 100));
                            }
                        });
                    }
                }
                currentPart.close();
                inputStream.close();
                if (canDown)
                    removeDownTask(context, this);  // 下载结束删除掉任务 手动暂定状态下不取消任务
                else
                    updateDownTask(context, this);  // 暂停状态下 更新下载进度到持久化 实现断点下载
                conn.disconnect();
            } catch (Exception e) {
                updateDownTask(context, this);  // 异常状态下 更新下载进度到持久化 实现断点下载
                e.printStackTrace();
            }
        }

        /**
         * 暂停下载 或者取消下载任务
         */
        public void closeDown(boolean removeTask) {
            canDown = false;
            if (removeTask) removeDownTask(context, this);  // 删除断点任务
        }

    }

}
