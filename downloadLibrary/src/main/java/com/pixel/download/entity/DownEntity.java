package com.pixel.download.entity;

import android.content.Context;
import android.database.Cursor;

import com.pixel.download.data.DataBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admiistrator on 2016/11/3.
 * <p>
 * 下载任务模型
 */

public class DownEntity {

    /*数据库编号*/
    private Long _id = 0L;
    /*下载地址*/
    private String downUrl = null;
    /*保存路径*/
    private String savePath = null;
    /*下载文件大小*/
    private Long fileSize = 0L;
    /*已经下载大小*/
    private Long downSize = 0L;

    public DownEntity() {
    }

    public DownEntity(long _id, String downUrl, String savePath, Long fileSize, Long downSize) {
        this._id = _id;
        this.downUrl = downUrl;
        this.savePath = savePath;
        this.fileSize = fileSize;
        this.downSize = downSize;
    }

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public String getDownUrl() {
        return downUrl;
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getDownSize() {
        return downSize;
    }

    public void setDownSize(Long downSize) {
        this.downSize = downSize;
    }

    @Override
    public String toString() {
        return "DownEntity{" +
                "_id=" + _id +
                ", downUrl='" + downUrl + '\'' +
                ", savePath='" + savePath + '\'' +
                ", fileSize=" + fileSize +
                ", downSize=" + downSize +
                '}';
    }

    public static DownEntity getEntity(Context context, String downUrl, String savePath) {
        List<DownEntity> entities = cursorToEntity(DataBase.executeQuery(
                context, " SELECT * FROM DownEntity WHERE downUrl = ? AND savePath = ? ", downUrl, savePath));
        if (entities != null && entities.size() > 0) {
            return entities.get(entities.size() - 1);
        }
        return null;
    }

    public static void saveEntity(Context context, DownEntity entity) {
        DataBase.executeUpdate(context,
                " INSERT INTO DownEntity (downUrl, savePath, fileSize, downSize) VALUES (?, ?, ?, ?) ",
                entity.getDownUrl(), entity.getSavePath(), entity.getFileSize().toString(), entity.getDownSize().toString());
    }

    public static void deleteEntity(Context context, String downUrl, String savePath) {
        DataBase.executeUpdate(context, " DELETE FROM DownEntity WHERE downUrl = ? AND savePath = ? ", downUrl, savePath);
    }

    public static void updateEntity(Context context, DownEntity entity) {
        DataBase.executeUpdate(context,
                " UPDATE DownEntity SET downUrl = ?, savePath = ?, fileSize = ? downSize = ? WHERE _id = ? ",
                entity.getDownUrl(), entity.getSavePath(), entity.getFileSize().toString(), entity.getDownSize().toString(), entity.get_id().toString());
    }

    public static void updateEntity(Context context, Long downSize, Long _id) {
        DataBase.executeUpdate(context,
                " UPDATE DownEntity SET downSize = ? WHERE _id = ? ", downSize.toString(), _id.toString());
    }

    private static List<DownEntity> cursorToEntity(Cursor cursor) {
        List<DownEntity> entities = new ArrayList<>();
        while (cursor.moveToNext()) {
            DownEntity entity = new DownEntity();
            entity.set_id(cursor.getLong(0));
            entity.setDownUrl(cursor.getString(1));
            entity.setSavePath(cursor.getString(2));
            entity.setFileSize(Long.parseLong(cursor.getString(3)));
            entity.setDownSize(Long.parseLong(cursor.getString(4)));
            entities.add(entity);
        }
        cursor.close();
        return entities;
    }

}
