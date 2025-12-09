package com.sunlight.invest.util;

import com.alibaba.druid.filter.config.ConfigTools;

public class DruidEncryptGenerator {

    /**
     * 生成加密密码和密钥对
     * @param args 命令行参数，第一个参数为待加密的密码（可选）
     */
    public static void main(String[] args) throws Exception {
        // 1. 获取待加密密码（支持命令行传入）
        String password = "Root@123456";
        if (args.length > 0) {
            password = args[0];
        }

        System.out.println("=====================================");
        System.out.println("待加密密码: " + password);
        System.out.println("=====================================\n");

        // 2. 生成 RSA 密钥对（512位）
        // keyPair[0] = 私钥（用于加密）
        // keyPair[1] = 公钥（用于解密）
        String[] keyPair = ConfigTools.genKeyPair(512);
        String privateKey = keyPair[0];
        String publicKey = keyPair[1];

        // 3. 使用私钥加密密码
        String encryptedPassword = ConfigTools.encrypt(privateKey, password);

        // 4. 输出结果
        System.out.println("【私钥 - 加密用】（无需保存到配置文件）：");
        System.out.println("privateKey:" + privateKey);
        System.out.println("\n【公钥 - 解密用】（需配置到 application.yml）：");
        System.out.println("publicKey:" + publicKey);
        System.out.println("\n【密文密码】（替换配置文件中的明文密码）：");
        System.out.println("password:" + encryptedPassword);

        // 5. 验证解密（可选）
        String decryptedPassword = ConfigTools.decrypt(publicKey, encryptedPassword);
        System.out.println("\n【验证解密】解密后密码: " + decryptedPassword);

        if (password.equals(decryptedPassword)) {
            System.out.println("\n✅ 加密解密验证成功！");
        } else {
            System.err.println("\n❌ 加密解密验证失败！");
        }
    }
}