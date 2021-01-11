package cn.kong.web.entity;

import lombok.Data;

/**
 * @author: gh
 * @date: 2021/03/12/0012 17:20
 * @description:
 */
@Data
public class AccessKey {
    int ak;
    public AccessKey(int seconds) {
        this.ak = seconds;
    }

    public static AccessKey withExpire(int seconds) {
        return new AccessKey(seconds);
    }
}
