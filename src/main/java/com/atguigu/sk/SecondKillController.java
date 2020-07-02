package com.atguigu.sk;

import com.atguigu.sk.utils.JedisPoolUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

//RestController 等价于Controller加上ResponseBody
@RestController
public class SecondKillController {
    //lua 脚本
    static String secKillScript = "local userid=KEYS[1];\r\n"
            + "local prodid=KEYS[2];\r\n"
            + "local qtkey='sk:'..prodid..\":qt\";\r\n"
            + "local usersKey='sk:'..prodid..\":usr\";\r\n"
            + "local userExists=redis.call(\"sismember\",usersKey,userid);\r\n"
            + "if tonumber(userExists)==1 then \r\n"
            + "   return 2;\r\n"
            + "end\r\n"
            + "local num= redis.call(\"get\" ,qtkey);\r\n"
            + "if tonumber(num)<=0 then \r\n"
            + "   return 0;\r\n"
            + "else \r\n"
            + "   redis.call(\"decr\",qtkey);\r\n"
            + "   redis.call(\"sadd\",usersKey,userid);\r\n"
            + "end\r\n"
            + "return 1";

    @PostMapping(value = "/sk/doSecondKill", produces = "text/html;charset=UTF-8")
    public String doSKByLUA(Integer id) {
        //随机生成用户id
        int userId = (int)(1000*Math.random());
        //Jedis jedis = new Jedis("192.168.80.128",6379);
        //从jedis连接池中获取一个连接,连接对象是被JedisPool代理过的连接对象
        Jedis jedis = JedisPoolUtil.getJedisPoolInstance().getResource();

        //加载LUA脚本
        String shal = jedis.scriptLoad(secKillScript);
        //将LUA脚本和LUA脚本需要的参数传给redis执行：keyCount：lua脚本需要的参数数量，params：参数列表
        Object obj = jedis.evalsha(shal, 2, userId + "", id + "");
        // Long 强转为Integer会报错  ，lang和Integer没有父类和子类的关系
        //被代理的jedis对象调用关闭方法，相当将jedis连接还给连接池
        jedis.close();
        int result = (int)(long)obj;
        if(result==1){
            System.out.println("秒杀成功" + userId);
            return "ok";
        }else if(result==2){
            System.out.println("重复秒杀");
            return "重复秒杀";
        }else{
            System.out.println("库存不足");
            return "库存不足";
        }

    }

/*=======分割线===================*/


        //@ResponseBody
    //@PostMapping(value = "/sk/doSecondKill",produces = "text/html;charset=UTF-8")
    public String doSecondKill(Integer id) {
        //随机生成用户id
        int userId = (int)(1000*Math.random());
        //秒杀商品id
        Integer pid = id;
        //System.out.println("pid = "+pid);

        //秒杀业务
        //拼接商品库存的key和用户列表集合的key
        String qtKey = "sk:"+pid+":qt";
        String usrKey = "sk:"+pid+":usr";

        //Jedis jedis = new Jedis("192.168.80.128",6379);
        Jedis jedis = new Jedis("139.224.135.236",6379);

        //1.判断用户是否秒杀过
        if (jedis.sismember(usrKey, userId + "")) {
            System.err.println("重复秒杀"+userId);
            return "该用户已参与秒杀，请勿重复秒杀";
        }
        //2.获取redis中的库存，判断是否足够
        //对库存的key进行watch  相当于加了乐观锁
        jedis.watch(qtKey);
        String qtStr = jedis.get(qtKey);
        System.out.println("库存 = " + qtStr);
        if (StringUtils.isEmpty(qtStr)) {
            System.err.println("尚未开始");
            return "秒杀尚未开始";
        }
        int qtNum = Integer.parseInt(qtStr);
        if (qtNum <= 0) {
            System.err.println("库存不足,已抢光");
            return "库存不足";
        }
        //3.库存足够，秒杀的业务
        //减库存
        //开始redis的组队（组队中的命令不会执行，而是添加到一个队列中）
        Transaction multi = jedis.multi();
        multi.decr(qtKey);
        //将用户加入到秒杀成功的列表中
        multi.sadd(usrKey, userId+"");
        //将队列中的所有命令交给redis执行（每个命令执行时和其他命令的成功失败没有关系）
        multi.exec();
        //discard：解散队列
        System.out.println("秒杀成功"+userId);
        //关闭
        jedis.close();
        return "ok";
    }

}
