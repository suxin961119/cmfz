package com.baizhi.aop;

import com.alibaba.fastjson.JSONObject;
import com.baizhi.redis.ClearRediscache;
import com.baizhi.redis.RedisCache;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Method;

@Configuration
@Aspect
public class RedisCacheAop {

    @Autowired
    private Jedis jedis;

    //环绕
    @Around("execution(* com.baizhi.service.*.selectAll (..))")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        //获取目标方法所在的类的对象
        Object target = proceedingJoinPoint.getTarget();
        //获取目标对象方法
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        //获取目标对象的参数值
        Object[] args = proceedingJoinPoint.getArgs();
        //拿到method
        Method method = signature.getMethod();
        boolean b = method.isAnnotationPresent(RedisCache.class);

        if (b) {
            //目标方法上存在注解
            //获取外出key
            String className = target.getClass().getName();
            //获取方法名
            String methodName = method.getName();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(methodName).append("(");
            for (int i = 0; i < args.length; i++) {
                stringBuilder.append(args[i]);
                if (i == args.length - 1) {
                    break;
                }
                stringBuilder.append(",");
            }
            stringBuilder.append(")");
            if (jedis.hexists(className, stringBuilder.toString())) {
                //判断redis中是否存在对应key
                String hget = jedis.hget(className, stringBuilder.toString());
                return JSONObject.parse(hget);
            } else {
                Object result = proceedingJoinPoint.proceed();
                jedis.hset(className, stringBuilder.toString(), JSONObject.toJSONString(result));
                return result;
            }


        } else {
            //目标方法没有注解
            Object proceed = proceedingJoinPoint.proceed();
            return proceed;
        }
    }

    //后置通知
    @After("execution(* com.baizhi.service.*.*(..)) && !execution(* com.baizhi.service.*.selectAll(..))")
    public void after(JoinPoint joinPoint) {
        //获取目标方法
        Object target = joinPoint.getTarget();
        String name = target.getClass().getName();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        boolean b = method.isAnnotationPresent(ClearRediscache.class);
        if (b) {
            //清除缓存
            jedis.del(name);
        }
    }
}
