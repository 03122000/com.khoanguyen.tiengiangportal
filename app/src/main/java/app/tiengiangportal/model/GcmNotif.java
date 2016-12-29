/*
Create by Nguyen Nguyen
 */
package app.tiengiangportal.model;

import java.io.Serializable;

public class GcmNotif implements Serializable{
    private String title, content;

    public GcmNotif() {
    }

    public GcmNotif(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
