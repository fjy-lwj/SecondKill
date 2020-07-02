package com.atguigu.sk.code;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

@RestController
@RequestMapping(value = "/code",produces = "text/html;charset=UTF-8")
public class VerifiCodeController {
    private Jedis jedis = new Jedis("192.168.80.128",6379);
    private String phone;

    @PostMapping("/getcode")
    public String getcode(String phone) {
        this.phone = phone;
        int code = (int) (Math.random() * (999999-100000+1) + 100000);

        jedis.setex(phone,120, code+"");
        jedis.setex(phone+"times", 120, "3");
        return code+"";
    }

    @GetMapping(value = "/confirmCode")
    public String confirmCode(String code, Model model) {
        String codeNum = jedis.get(phone);
        if (codeNum.equals(code)) {
            return "验证成功";
        } else {
            int time = Integer.parseInt(jedis.get(phone + "times"));
            if (time <= 0) {
                model.addAttribute("error", "3次机会已用尽");
                return "3次机会已用尽";
            }
            jedis.decr(phone+"times");
            model.addAttribute("error", "输入错误，请重新输入");
            return "验证失败";

        }


    }

}
