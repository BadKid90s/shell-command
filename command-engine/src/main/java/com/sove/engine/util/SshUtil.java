package com.sove.engine.util;

import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.JSchException;

public class SshUtil {

    public static boolean joinable(String host, Integer port, String user, String password) {
        try {
            JschUtil.openSession(host, port, user, password).connect();
            return true;
        } catch (JSchException e) {
            return false;
        }
    }
}
