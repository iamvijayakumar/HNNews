package com.hnnews.news.pojo;

/**
 * Created by VIJAYAKUMAR MUNIAPPA on 07-09-2016.
 */

public class News {


    public String title;
    public String url;
    public String id;


    /**
     * @return The feed title
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title The feed title
     */
    public void setTitle(String title) {
        this.title = title;
    }
    /**
     * @return The feed url
     */
    public String getUrl() {
        return url;
    }
    /**
     * @param url The feed url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return The top stories id
     */
    public String getId() {
        return id;
    }
    /**
     * @param id top stories id
     */
    public void setId(String id) {
        this.id = id;
    }
}
