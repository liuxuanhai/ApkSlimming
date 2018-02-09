package com.pf.res_guard_core;

import com.pf.res_guard_core.obfuscate.Client;
import java.io.IOException;
/**
 * @author zhaopf
 * @version 1.0
 * @QQ 1308108803
 * @date 2018/2/9
 */
public class Main {

    public static void main(String[] args) {
        try {
            Client.guard();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}