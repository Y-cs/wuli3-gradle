package com.kjs.wuli3.generator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * 生成单体多模块 DDD 业务服务骨架的命令行入口。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
public final class DddGenerator {
    private DddGenerator() {}

    /**
     * 解析命令行参数并生成业务服务。
     *
     * @param args 命令行参数
     * @throws IOException 生成文件失败
     */
    public static void main(final String[] args) throws IOException {
        if (args.length == 0 || "--help".equals(args[0]) || "-h".equals(args[0])) {
            DddGenerator.printUsage();
            return;
        }
        if (!"generate".equals(args[0])) {
            throw new IllegalArgumentException("仅支持 generate 命令");
        }
        final GeneratorOptions options = GeneratorOptions.parse(Arrays.copyOfRange(args, 1, args.length));
        new DddProjectGenerator().generate(options);
    }

    private static void printUsage() {
        System.out.println("Usage: generate --service NAME --package PACKAGE --domain NAME "
                + "[--persistence none|mysql] [--messaging none|rocketmq|rabbitmq] "
                + "[--wuli3-version VERSION] [--build-logic-version VERSION] [--output DIR]");
    }

    /** 供测试和嵌入式调用使用的生成入口。 */
    static Path generate(final GeneratorOptions options) throws IOException {
        return new DddProjectGenerator().generate(options);
    }
}
