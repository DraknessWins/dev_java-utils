import java.util.Random;

/**
 * Twitter分布式自增ID
 *
 * @author 散入风中
 * @create 2018-11-20  10点44分
 *
 * 特点：1.按时间有序生成
 *       2.64bit大小整数，标记型ID
 *       3.分布式系统内不会出现重复ID（使用datacenterId和workerId做区分）
 * */
public class SnowFlake {
    /**
     * 运行起始时间戳，可进行更改
     * 2018 - 11 - 20
     * */
    private final static long START_STMP = 1542643200000L;

    /**
     * 每部分占用的位数
     **/
    private final static long SEQUENCE_BIT = 12;  //序列号占用位数,12bit
    private final static long MACHINE_BIT = 5;    //机器标识占用位数,5bit
    private final static long DATACENTER_BIT = 5; //数据中心占用位数,5bit

    /**
     * 每部分的最大值
     **/
    //最大 31
    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    //最大 31
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    //最大 4095
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    /**
     * 每部分向左位移
     * */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;                          // 序列号标识
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;         // 机器标识
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;     // 时间戳标识

    private long datacenterId;      //数据中心标识
    private long machineId;         //机器标识
    private long sequence = 0L;     //序列号
    private long lastStmp = -1L;    //上一次生成ID的时间戳

    /**
     * 使用双ID区别集群机器ID，每毫秒集群机器ID之间保证差异性
     * */
    public SnowFlake(long datacenterId, long machineId){
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0){
            throw new IllegalArgumentException("数据中心标识超过最大值上限或小于0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0){
            throw new IllegalArgumentException("机器标识超过最大值上限或小于0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * 雪花算法
     * */
    public synchronized long nextId(){
        long currStmp = getNewstmp();
        if (currStmp < lastStmp){
            throw new RuntimeException("时间回退，系统时间戳小于上一次生成ID时间戳，生成ID失败");
        }
        if (currStmp == lastStmp){
            //同毫秒内序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同毫秒序列数已经达到最大，获取下一个时间戳标记
            if (sequence == 0L){
                currStmp = getNextMill();
            }
        }else {
            //不同毫秒内，序列号置0，出于取模随机性，应使用(伪)随机数
            /*sequence = 0L;*/
            Random random = new Random();
            random.nextInt(10);
        }
        lastStmp = currStmp;

        return (currStmp - START_STMP) << TIMESTMP_LEFT     //时间戳部分
                |   datacenterId << DATACENTER_LEFT         //数据中心部分
                |   machineId << MACHINE_LEFT               //机器标识部分
                |   sequence;                               //序列号部分
    }

    private long getNextMill(){
        long mill = getNewstmp();
        while(mill <= lastStmp){
            mill = getNewstmp();
        }
        return mill;
    }

    /**
     * 获取下一个时间戳，该方法不通过使用机器系统时间获取当前时间，其实是存在相同ID的可能的
     */
    private long getNewstmp(){
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        SnowFlake snowFlake = new SnowFlake(1, 10);
        for (int i = 0; i < 100000; i++){
            System.out.println(snowFlake.nextId());
        }
    }
}
