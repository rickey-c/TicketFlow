package com.ticketflow.lockinfo;

import com.ticketflow.core.SpringUtil;
import com.ticketflow.parser.ExtParameterNameDiscoverer;
import com.ticketflow.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.ticketflow.constant.Constants.SEPARATOR;

/**
 * @Description: 锁信息抽象类，提供了锁名称的生成、SpEL表达式的解析等功能。
 * @Author: rickey-c
 * @Date: 2025/1/26 14:40
 */
@Slf4j
public abstract class AbstractLockInfoHandle implements LockInfoHandle {
    // 分布式锁名称前缀
    private static final String LOCK_DISTRIBUTE_ID_NAME_PREFIX = "lock_distribute_id";

    // 用于获取方法参数名称的工具类
    private final ParameterNameDiscoverer nameDiscoverer = new ExtParameterNameDiscoverer();

    // SpEL表达式解析器
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 获取锁信息前缀，由子类实现
     *
     * @return 锁信息前缀
     */
    protected abstract String getLockPrefixName();

    /**
     * 获取锁名称
     * 生成锁的全名，包括前缀、锁的名称和自定义键值
     *
     * @param joinPoint 切入点，用于获取方法参数等信息
     * @param name      锁的名称
     * @param keys      锁的自定义键
     * @return 完整的锁名称
     */
    @Override
    public String getLockName(JoinPoint joinPoint, String name, String[] keys) {
        // 拼接最终的锁名称
        return SpringUtil.getPrefixDistinctionName() +
                "-" +
                getLockPrefixName() +
                SEPARATOR +
                name +
                getRelKey(joinPoint, keys);
    }

    /**
     * 获取简单的锁名称
     * 该方法用于生成一个简单的锁名称，主要根据指定的名称和键来拼接
     *
     * @param name 锁的名称
     * @param keys 锁的自定义键
     * @return 锁名称
     */
    @Override
    public String simpleGetLockName(String name, String[] keys) {
        List<String> definitionKeyList = new ArrayList<>();
        // 过滤掉空的键
        for (String key : keys) {
            if (StringUtil.isNotEmpty(key)) {
                definitionKeyList.add(key);
            }
        }
        // 拼接并返回简单的锁名称
        return SpringUtil.getPrefixDistinctionName() + "-" +
                LOCK_DISTRIBUTE_ID_NAME_PREFIX + SEPARATOR + name + SEPARATOR + String.join(SEPARATOR, definitionKeyList);
    }

    /**
     * 获取自定义的锁键
     * 根据切入点的参数和传入的自定义键，通过SpEL表达式生成锁的键
     *
     * @param joinPoint 切入点对象，用于获取方法和方法参数
     * @param keys      自定义键
     * @return 拼接后的自定义锁键
     */
    private String getRelKey(JoinPoint joinPoint, String[] keys) {
        Method method = getMethod(joinPoint);
        List<String> definitionKeys = getSpElKey(keys, method, joinPoint.getArgs());
        // 使用分隔符拼接自定义键
        return SEPARATOR + String.join(SEPARATOR, definitionKeys);
    }

    /**
     * 获取目标方法
     * 通过 JoinPoint 获取方法签名，如果方法是接口方法，则通过目标对象获取实现方法
     *
     * @param joinPoint 切入点
     * @return 目标方法
     */
    private Method getMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 如果是接口方法，则从目标类获取实现方法
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(signature.getName(),
                        method.getParameterTypes());
            } catch (Exception e) {
                log.error("get method error ", e);
            }
        }
        return method;
    }

    /**
     * 使用SpEL表达式解析自定义的锁键
     * 通过SpEL表达式解析自定义的键，结合方法参数，动态生成锁键
     *
     * @param definitionKeys  锁键的SpEL表达式数组
     * @param method          当前方法
     * @param parameterValues 方法的参数值
     * @return 解析后的锁键列表
     */
    private List<String> getSpElKey(String[] definitionKeys, Method method, Object[] parameterValues) {
        List<String> definitionKeyList = new ArrayList<>();
        for (String definitionKey : definitionKeys) {
            if (!ObjectUtils.isEmpty(definitionKey)) {
                // 创建EvaluationContext用于解析SpEL表达式
                EvaluationContext context = new MethodBasedEvaluationContext(
                        null,
                        method,
                        parameterValues,
                        nameDiscoverer);
                // 解析SpEL表达式
                Object objKey = parser.parseExpression(definitionKey).getValue(context);
                // 将解析后的值加入到列表中
                definitionKeyList.add(ObjectUtils.nullSafeToString(objKey));
            }
        }
        return definitionKeyList;
    }

}
