package com.example.ecrhub.pojo;

/**
 * @author: yanzx
 * @date: 2023/11/10 13:45
 * @description:
 */
public class ECRDebugPo {

    private String send_raw;

    private String send_pretty;

    private String receive_raw;

    private String receive_pretty;

    public String getSend_raw() {
        return send_raw;
    }

    public void setSend_raw(String send_raw) {
        this.send_raw = send_raw;
    }

    public String getSend_pretty() {
        return send_pretty;
    }

    public void setSend_pretty(String send_pretty) {
        this.send_pretty = send_pretty;
    }

    public String getReceive_raw() {
        return receive_raw;
    }

    public void setReceive_raw(String receive_raw) {
        this.receive_raw = receive_raw;
    }

    public String getReceive_pretty() {
        return receive_pretty;
    }

    public void setReceive_pretty(String receive_pretty) {
        this.receive_pretty = receive_pretty;
    }
}
