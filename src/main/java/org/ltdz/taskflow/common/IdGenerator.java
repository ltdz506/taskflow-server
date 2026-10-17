package org.ltdz.taskflow.common;

import lombok.experimental.UtilityClass;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 时间戳 ID 生成器。
 * <p>
 * 生成 64 位十进制 long，位布局如下（最高位固定 0，保证为正数）：
 * <pre>
 *   bit 63     : 0（符号位）
 *   bit 62..21 : 42 位毫秒时间戳（可用于 2109 年前）
 *   bit 20..11 : 10 位自增序列（0-1023，同一毫秒内递增）
 *   bit 10..0  : 11 位局域网信息哈希（0-2047）
 * </pre>
 * 示例：192.168.0.125 机器上生成的 ID 为 3758549389012897407
 */
@UtilityClass
public class IdGenerator {

    /** 42 位时间戳掩码 */
    private static final long TIME_MASK = 0x3FFFFFFFFFFL;

    /** 10 位序列掩码 */
    private static final long SEQUENCE_MASK = 0x3FFL;

    /** 11 位机器哈希掩码 */
    private static final int MACHINE_MASK = 0x7FF;

    /** 自增序列 */
    private static final AtomicLong SEQUENCE = new AtomicLong(0L);

    /** 本机局域网信息哈希（11 位），类加载时解析一次 */
    private static final int MACHINE_HASH = resolveMachineHash();

    /**
     * 生成一个 64 位十进制 ID（最高位为 0，保证为正数）。
     *
     * @return 十进制 long，可直接存入 BIGINT
     */
    public static long next() {
        long timestamp = System.currentTimeMillis() & TIME_MASK;
        long sequence = SEQUENCE.getAndIncrement() & SEQUENCE_MASK;
        return (timestamp << 21)
                | (sequence << 11)
                | MACHINE_HASH;
    }

    /**
     * 计算本机局域网 IPv4 的 11 位哈希；取不到网卡时退回随机值。
     */
    private static int resolveMachineHash() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        byte[] bytes = address.getAddress();
                        int ip = ((bytes[0] & 0xFF) << 24)
                                | ((bytes[1] & 0xFF) << 16)
                                | ((bytes[2] & 0xFF) << 8)
                                | (bytes[3] & 0xFF);
                        // 异或折叠：把 32 位 IP 压缩到 11 位
                        return (ip ^ (ip >>> 11) ^ (ip >>> 22)) & MACHINE_MASK;
                    }
                }
            }
        } catch (Exception ignored) {
            // 取不到网卡信息时使用随机兜底
        }
        return ThreadLocalRandom.current().nextInt() & MACHINE_MASK;
    }
}
