package com.ticketflow.toolkit;

import cn.hutool.core.date.SystemClock;
import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Description: 雪花算法生成器
 * @Author: rickey-c
 * @Date: 2025/1/26 12:53
 */
@Slf4j
public class SnowflakeIdGenerator {

    // 基础时间
    private static final long BASIS_TIME = 1288834974657L;

    // 位数和偏移量
    private final long workerIdBits = 5L;
    private final long datacenterIdBits = 5L;
    private final long sequenceBits = 12L;
    private final long workerIdShift = sequenceBits;
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    // 最大值
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    private final long workerId;

    private final long datacenterId;

    private long sequence = 0L;

    private long lastTimestamp = -1L;

    private InetAddress inetAddress;

    /**
     * 雪花Id生成构造器
     *
     * @param workDataCenterId
     */
    public SnowflakeIdGenerator(WorkDataCenterId workDataCenterId) {
        if (Objects.nonNull(workDataCenterId.getDataCenterId())) {
            this.workerId = workDataCenterId.getWorkId();
            this.datacenterId = workDataCenterId.getDataCenterId();
        } else {
            this.datacenterId = getDatacenterId(maxDatacenterId);
            this.workerId = getMaxWorkerId(datacenterId, maxWorkerId);
        }
    }

    public SnowflakeIdGenerator(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
        this.datacenterId = getDatacenterId(maxDatacenterId);
        this.workerId = getMaxWorkerId(datacenterId, maxWorkerId);
        initLog();
    }

    /**
     * 初始化
     */
    private void initLog() {
        if (log.isDebugEnabled()) {
            log.debug("Initialization SnowflakeIdGenerator datacenterId:{} workerId:{}",
                    this.datacenterId, this.workerId);
        }
    }

    /**
     * 通过workId和dataCenterId初始化
     * @param workerId
     * @param datacenterId
     */
    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        Assert.isFalse(workerId > maxWorkerId || workerId < 0,
                String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        Assert.isFalse(datacenterId > maxDatacenterId || datacenterId < 0,
                String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        initLog();
    }

    /**
     * 获取当前时间
     * @return
     */
    public long getBase() {
        int five = 5;
        long timestamp = timeGen();
        // 发生了时钟漂移，判断偏移量，然后尝试再次获取
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= five) {
                try {
                    wait(offset << 1);
                    timestamp = timeGen();
                    if (timestamp < lastTimestamp) {
                        throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", offset));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", offset));
            }
        }

        if (lastTimestamp == timestamp) {
            // 相同毫秒内，序列号自增
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                // 同一毫秒的序列数已经达到最大
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒内，序列号置为 1 - 2 随机数
            sequence = ThreadLocalRandom.current().nextLong(1, 3);
        }

        lastTimestamp = timestamp;

        return timestamp;
    }


    protected long getMaxWorkerId(long datacenterId, long maxWorkerId) {
        StringBuilder mpid = new StringBuilder();
        // 把dataCenterId作为前缀
        mpid.append(datacenterId);
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (StringUtils.isNotBlank(name)) {
            // 获取当前JVM进程Id
            mpid.append(name.split("@")[0]);
        }
        // 计算哈希取模
        return (mpid.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }

    protected long getDatacenterId(long maxDatacenterId) {
        long id = 0L;
        try {
            if (null == this.inetAddress) {
                this.inetAddress = InetAddress.getLocalHost();  // 获取本地 IP 地址
            }
            NetworkInterface network = NetworkInterface.getByInetAddress(this.inetAddress);  // 获取网络接口
            if (null == network) {
                id = 1L;  // 如果未找到网络接口，默认 ID 为 1
            } else {
                byte[] mac = network.getHardwareAddress();  // 获取 MAC 地址
                if (null != mac) {
                    id = ((0x000000FF & (long) mac[mac.length - 2]) |  // MAC 倒数第二个字节
                            (0x0000FF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;  // MAC 最后一个字节
                    id = id % (maxDatacenterId + 1);  // 取模确保范围
                }
            }
        } catch (Exception e) {
            log.warn(" getDatacenterId: {}", e.getMessage());
        }
        return id;
    }


    public synchronized long nextId() {
        long timestamp = getBase();

        return ((timestamp - BASIS_TIME) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }

    public synchronized long getOrderNumber(long userId, long tableCount) {
        long timestamp = getBase();
        long sequenceShift = log2N(tableCount);
        return ((timestamp - BASIS_TIME) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | (sequence << sequenceShift)
                | (userId % tableCount);
    }

    /**
     * 尝试再次生成时间戳
     * @param lastTimestamp
     * @return
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return SystemClock.now();
    }

    public static long parseIdTimestamp(long id) {
        return (id >> 22) + BASIS_TIME;
    }

    public long log2N(long count) {
        return (long) (Math.log(count) / Math.log(2));
    }

    public long getMaxWorkerId() {
        return maxWorkerId;
    }

    public long getMaxDatacenterId() {
        return maxDatacenterId;
    }

}
