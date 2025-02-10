package com.ticketflow.impl.composite;


import com.ticketflow.enums.BaseCode;
import com.ticketflow.exception.TicketFlowFrameException;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 组合模式容器
 * @Author: rickey-c
 * @Date: 2025/1/30 23:39
 */
public class CompositeContainer<T> {

    private final Map<String, AbstractComposite> allCompositeInterfaceMap = new HashMap<>();

    public void init(ConfigurableApplicationContext applicationEvent) {
        Map<String, AbstractComposite> compositeInterfaceMap = applicationEvent.getBeansOfType(AbstractComposite.class);

        // 通过业务类型分类
        Map<String, List<AbstractComposite>> collect = compositeInterfaceMap.values().stream().collect(Collectors.groupingBy(AbstractComposite::type));
        collect.forEach((k, v) -> {
            // 构建组合校验树
            AbstractComposite root = build(v);
            if (Objects.nonNull(root)) {
                // 根据类型放到校验树的map中，可以通过类型取到校验树
                allCompositeInterfaceMap.put(k, root);
            }
        });
    }

    /**
     * 执行组合校验逻辑
     *
     * @param type  组合类型
     * @param param 执行需要的参数
     */
    public void execute(String type, T param) {
        // 从map中取出对应类型的校验树，根据入参执行
        AbstractComposite compositeInterface = Optional.ofNullable(allCompositeInterfaceMap.get(type))
                .orElseThrow(() -> new TicketFlowFrameException(BaseCode.COMPOSITE_NOT_EXIST));
        compositeInterface.allExecute(param);
    }

    /**
     * 构建组件树的辅助方法。
     *
     * @param groupedByTier 按层级组织的组件映射。
     * @param currentTier   当前处理的层级。
     */
    private static void buildTree(Map<Integer, Map<Integer, AbstractComposite>> groupedByTier, int currentTier) {
        Map<Integer, AbstractComposite> currentLevelComponents = groupedByTier.get(currentTier);
        Map<Integer, AbstractComposite> nextLevelComponents = groupedByTier.get(currentTier + 1);

        if (currentLevelComponents == null) {
            return;
        }

        if (nextLevelComponents != null) {
            for (AbstractComposite child : nextLevelComponents.values()) {
                Integer parentOrder = child.executeParentOrder();
                if (parentOrder == null || parentOrder == 0) {
                    continue;
                }
                AbstractComposite parent = currentLevelComponents.get(parentOrder);
                if (parent != null) {
                    parent.add(child);
                }
            }
        }
        buildTree(groupedByTier, currentTier + 1);
    }

    /**
     * 根据提供的组件集合构建组件树，并返回根节点。
     *
     * @param components 组件集合。
     * @return 根节点。
     */
    private static AbstractComposite build(Collection<AbstractComposite> components) {
        Map<Integer, Map<Integer, AbstractComposite>> groupedByTier = new TreeMap<>();

        for (AbstractComposite component : components) {
            groupedByTier.computeIfAbsent(component.executeTier(), k -> new HashMap<>(16))
                    .put(component.executeOrder(), component);
        }

        Integer minTier = groupedByTier.keySet().stream().min(Integer::compare).orElse(null);
        if (minTier == null) {
            return null;
        }

        buildTree(groupedByTier, minTier);

        return groupedByTier.get(minTier).values().stream()
                .filter(c -> c.executeParentOrder() == null || c.executeParentOrder() == 0)
                .findFirst()
                .orElse(null);
    }
}
