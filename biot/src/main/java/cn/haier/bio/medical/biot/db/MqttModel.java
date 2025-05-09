package cn.haier.bio.medical.biot.db;

public class MqttModel {
    private int id;
    private String time;
    private String content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "MqttModel{" +
                "id=" + id +
                ", time='" + time + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
