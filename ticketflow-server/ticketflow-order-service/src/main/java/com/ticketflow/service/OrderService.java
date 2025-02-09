package com.ticketflow.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.damai.enums.PayBillStatus;
import com.damai.enums.PayChannel;
import com.ticketflow.utils.StringUtil;
import com.ticketflow.client.PayClient;
import com.ticketflow.client.UserClient;
import com.ticketflow.common.ApiResponse;
import com.ticketflow.core.RedisKeyManage;
import com.ticketflow.dto.*;
import com.ticketflow.entity.Order;
import com.ticketflow.entity.OrderTicketUser;
import com.ticketflow.entity.OrderTicketUserAggregate;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.enums.BusinessStatus;
import com.ticketflow.enums.OrderStatus;
import com.ticketflow.enums.SellStatus;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.mapper.OrderMapper;
import com.ticketflow.mapper.OrderTicketUserMapper;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.redis.RedisKeyBuild;
import com.ticketflow.repeatexecutelimit.annotion.RepeatExecuteLimit;
import com.ticketflow.request.CustomizeRequestWrapper;
import com.ticketflow.service.delaysend.DelayOperateProgramDataSend;
import com.ticketflow.service.properties.OrderProperties;
import com.ticketflow.servicelock.LockType;
import com.ticketflow.servicelock.annotion.ServiceLock;
import com.ticketflow.util.ServiceLockTool;
import com.ticketflow.utils.DateUtils;
import com.ticketflow.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.damai.constant.Constant.ALIPAY_NOTIFY_SUCCESS_RESULT;
import static com.ticketflow.core.DistributedLockConstants.*;
import static com.ticketflow.core.RepeatExecuteLimitConstants.*;

/**
 * @Description: 订单 service
 * @Author: rickey-c
 * @Date: 2025/2/9 17:12
 */
@Slf4j
@Service
public class OrderService {

    @Autowired
    private UidGenerator uidGenerator;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderTicketUserMapper orderTicketUserMapper;

    @Autowired
    private OrderTicketUserService orderTicketUserService;

    @Autowired
    private OrderProgramCacheResolutionOperate orderProgramCacheResolutionOperate;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private PayClient payClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    private OrderProperties orderProperties;

    @Lazy
    @Autowired
    private OrderService orderService;

    @Autowired
    private DelayOperateProgramDataSend delayOperateProgramDataSend;

    @Autowired
    private ServiceLockTool serviceLockTool;


    @Transactional(rollbackFor = Exception.class)
    public String create(OrderCreateDto orderCreateDto) {
        LambdaQueryWrapper<Order> orderLambdaQueryWrapper =
                Wrappers.lambdaQuery(Order.class).eq(Order::getOrderNumber, orderCreateDto.getOrderNumber());
        Order oldOrder = orderMapper.selectOne(orderLambdaQueryWrapper);
        // 验证订单是否已存在
        if (Objects.nonNull(oldOrder)) {
            throw new TicketFlowFrameException(BaseCode.ORDER_EXIST);
        }
        Order order = new Order();
        BeanUtil.copyProperties(orderCreateDto, order);
        order.setDistributionMode("电子票");
        order.setTakeTicketMode("请使用购票人身份证直接入场");
        List<OrderTicketUser> orderTicketUserList = new ArrayList<>();
        for (OrderTicketUserCreateDto orderTicketUserCreateDto : orderCreateDto.getOrderTicketUserCreateDtoList()) {
            OrderTicketUser orderTicketUser = new OrderTicketUser();
            BeanUtil.copyProperties(orderTicketUserCreateDto, orderTicketUser);
            orderTicketUser.setId(uidGenerator.getUid());
            orderTicketUserList.add(orderTicketUser);
        }
        orderMapper.insert(order);
        // 修改购票人订单数据
        orderTicketUserService.saveBatch(orderTicketUserList);
        // 更新redis的购票人订单数据
        redisCache.incrBy(RedisKeyBuild.createRedisKey(
                        RedisKeyManage.ACCOUNT_ORDER_COUNT,
                        orderCreateDto.getUserId(),
                        orderCreateDto.getProgramId()),
                orderCreateDto.getOrderTicketUserCreateDtoList().size());
        return String.valueOf(order.getOrderNumber());
    }

    @RepeatExecuteLimit(name = CANCEL_PROGRAM_ORDER, keys = {"#orderCancelDto.orderNumber"})
    @ServiceLock(name = ORDER_CANCEL_LOCK, keys = {"#orderCancelDto.orderNumber"})
    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(OrderCancelDto orderCancelDto) {
        updateOrderRelatedData(orderCancelDto.getOrderNumber(), OrderStatus.CANCEL);
        return true;
    }

    /**
     * 更新订单和购票人订单状态以及操作缓存数据
     *
     * @param orderNumber
     * @param orderStatus
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderRelatedData(Long orderNumber, OrderStatus orderStatus) {
        //如果不是取消或者支付操作，则直接抛出异常提示
        if (!(Objects.equals(orderStatus.getCode(), OrderStatus.CANCEL.getCode()) ||
                Objects.equals(orderStatus.getCode(), OrderStatus.PAY.getCode()))) {
            throw new TicketFlowFrameException(BaseCode.OPERATE_ORDER_STATUS_NOT_PERMIT);
        }
        //查询订单
        LambdaQueryWrapper<Order> orderLambdaQueryWrapper =
                Wrappers.lambdaQuery(Order.class).eq(Order::getOrderNumber, orderNumber);
        Order order = orderMapper.selectOne(orderLambdaQueryWrapper);
        //检查订单的状态 已取消、已支付、已退单的状态不再执行
        checkOrderStatus(order);
        //将订单更新为取消或者支付状态
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setOrderStatus(orderStatus.getCode());

        //将购票人订单更新为取消或者支付状态
        OrderTicketUser updateOrderTicketUser = new OrderTicketUser();
        updateOrderTicketUser.setOrderStatus(orderStatus.getCode());
        if (Objects.equals(orderStatus.getCode(), OrderStatus.PAY.getCode())) {
            updateOrder.setPayOrderTime(DateUtils.now());
            updateOrderTicketUser.setPayOrderTime(DateUtils.now());
        } else if (Objects.equals(orderStatus.getCode(), OrderStatus.CANCEL.getCode())) {
            updateOrder.setCancelOrderTime(DateUtils.now());
            updateOrderTicketUser.setCancelOrderTime(DateUtils.now());
        }
        //更新订单
        LambdaUpdateWrapper<Order> orderLambdaUpdateWrapper =
                Wrappers.lambdaUpdate(Order.class).eq(Order::getOrderNumber, order.getOrderNumber());
        int updateOrderResult = orderMapper.update(updateOrder, orderLambdaUpdateWrapper);

        //更新购票人订单
        LambdaUpdateWrapper<OrderTicketUser> orderTicketUserLambdaUpdateWrapper =
                Wrappers.lambdaUpdate(OrderTicketUser.class).eq(OrderTicketUser::getOrderNumber, order.getOrderNumber());
        int updateTicketUserOrderResult =
                orderTicketUserMapper.update(updateOrderTicketUser, orderTicketUserLambdaUpdateWrapper);
        if (updateOrderResult <= 0 || updateTicketUserOrderResult <= 0) {
            throw new com.ticketflow.exception.TicketFlowFrameException(BaseCode.ORDER_CANAL_ERROR);
        }
        //查询该订单下的购票人订单列表
        LambdaQueryWrapper<OrderTicketUser> orderTicketUserLambdaQueryWrapper =
                Wrappers.lambdaQuery(OrderTicketUser.class).eq(OrderTicketUser::getOrderNumber, order.getOrderNumber());
        List<OrderTicketUser> orderTicketUserList = orderTicketUserMapper.selectList(orderTicketUserLambdaQueryWrapper);
        if (CollectionUtil.isEmpty(orderTicketUserList)) {
            throw new TicketFlowFrameException(BaseCode.TICKET_USER_ORDER_NOT_EXIST);
        }
        Long programId = order.getProgramId();
        Map<Long, List<OrderTicketUser>> orderTicketUserSeatList =
                orderTicketUserList.stream().collect(Collectors.groupingBy(OrderTicketUser::getTicketCategoryId));
        //查询到购票人的座位
        //seatMap key:票档id value:座位id集合
        Map<Long, List<Long>> seatMap = new HashMap<>(orderTicketUserSeatList.size());
        orderTicketUserSeatList.forEach((k, v) -> {
            seatMap.put(k, v.stream().map(OrderTicketUser::getSeatId).collect(Collectors.toList()));
        });
        //更新缓存相关数据
        updateProgramRelatedDataResolution(programId, seatMap, orderStatus);
    }

    public void checkOrderStatus(Order order) {
        if (Objects.isNull(order)) {
            throw new TicketFlowFrameException(BaseCode.ORDER_NOT_EXIST);
        }
        if (Objects.equals(order.getOrderStatus(), com.ticketflow.enums.OrderStatus.CANCEL.getCode())) {
            throw new TicketFlowFrameException(BaseCode.ORDER_CANCEL);
        }
        if (Objects.equals(order.getOrderStatus(), com.ticketflow.enums.OrderStatus.PAY.getCode())) {
            throw new TicketFlowFrameException(BaseCode.ORDER_PAY);
        }
        if (Objects.equals(order.getOrderStatus(), com.ticketflow.enums.OrderStatus.REFUND.getCode())) {
            throw new TicketFlowFrameException(BaseCode.ORDER_REFUND);
        }
    }

    public void updateProgramRelatedDataResolution(Long programId, Map<Long, List<Long>> seatMap, OrderStatus orderStatus) {
        Map<Long, List<SeatVo>> seatVoMap = new HashMap<>(seatMap.size());
        //从redis中查询锁定中的座位
        seatMap.forEach((k, v) -> {
            seatVoMap.put(k, redisCache.multiGetForHash(
                    RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_LOCK_RESOLUTION_HASH, programId, k),
                    v.stream().map(String::valueOf).collect(Collectors.toList()), SeatVo.class));

        });
        if (CollectionUtil.isEmpty(seatVoMap)) {
            throw new TicketFlowFrameException(BaseCode.LOCK_SEAT_LIST_EMPTY);
        }
        //票档相关数据
        JSONArray jsonArray = new JSONArray();
        //要添加的座位相关数据
        JSONArray addSeatDatajsonArray = new JSONArray();
        List<TicketCategoryCountDto> ticketCategoryCountDtoList = new ArrayList<>(seatVoMap.size());
        //锁定的座位相关数据
        JSONArray unLockSeatIdjsonArray = new JSONArray();
        //锁定的座位id集合(用于发送给节目服务使用)
        List<Long> unLockSeatIdList = new ArrayList<>();
        seatVoMap.forEach((k, v) -> {
            JSONObject unLockSeatIdjsonObject = new JSONObject();
            //锁定的座位hash的key
            unLockSeatIdjsonObject.put("programSeatLockHashKey", RedisKeyBuild.createRedisKey(
                    RedisKeyManage.PROGRAM_SEAT_LOCK_RESOLUTION_HASH, programId, k).getRelKey());
            //扣除锁定的座位数据
            unLockSeatIdjsonObject.put("unLockSeatIdList", v.stream()
                    .map(SeatVo::getId).map(String::valueOf).collect(Collectors.toList()));
            unLockSeatIdjsonArray.add(unLockSeatIdjsonObject);

            JSONObject seatDatajsonObject = new JSONObject();
            //要添加的座位hash的key
            String seatHashKeyAdd = "";
            //如果是订单取消操作
            if (Objects.equals(orderStatus.getCode(), OrderStatus.CANCEL.getCode())) {
                //要添加的座位hash的key就是未售卖座位
                seatHashKeyAdd = RedisKeyBuild.createRedisKey(
                        RedisKeyManage.PROGRAM_SEAT_NO_SOLD_RESOLUTION_HASH, programId, k).getRelKey();
                for (SeatVo seatVo : v) {
                    //座位状态要改成未售卖
                    seatVo.setSellStatus(SellStatus.NO_SOLD.getCode());
                }
                //如果是订单支付操作
            } else if (Objects.equals(orderStatus.getCode(), OrderStatus.PAY.getCode())) {
                //要添加的座位hash的key就是已售卖座位
                seatHashKeyAdd = RedisKeyBuild.createRedisKey(
                        RedisKeyManage.PROGRAM_SEAT_SOLD_RESOLUTION_HASH, programId, k).getRelKey();
                for (SeatVo seatVo : v) {
                    //座位状态要改成已售卖
                    seatVo.setSellStatus(SellStatus.SOLD.getCode());
                }
            }
            seatDatajsonObject.put("seatHashKeyAdd", seatHashKeyAdd);
            List<String> seatDataList = new ArrayList<>();
            for (SeatVo seatVo : v) {
                seatDataList.add(String.valueOf(seatVo.getId()));
                seatDataList.add(JSON.toJSONString(seatVo));
            }
            //如果是订单取消的操作，那么添加到未售卖的座位数据
            //如果是订单支付的操作，那么添加到已售卖的座位数据
            seatDatajsonObject.put("seatDataList", seatDataList);
            addSeatDatajsonArray.add(seatDatajsonObject);

            //票档相关数据（只在订单取消操作有用）
            JSONObject jsonObject = new JSONObject();
            //票档的hash的key
            jsonObject.put("programTicketRemainNumberHashKey", RedisKeyBuild.createRedisKey(
                    RedisKeyManage.PROGRAM_TICKET_REMAIN_NUMBER_HASH_RESOLUTION, programId, k).getRelKey());
            //票档id
            jsonObject.put("ticketCategoryId", String.valueOf(k));
            //票档恢复的余票数量
            jsonObject.put("count", v.size());
            jsonArray.add(jsonObject);

            //组装发送给节目服务的数据
            TicketCategoryCountDto ticketCategoryCountDto = new TicketCategoryCountDto();
            ticketCategoryCountDto.setTicketCategoryId(k);
            ticketCategoryCountDto.setCount((long) v.size());
            ticketCategoryCountDtoList.add(ticketCategoryCountDto);

            unLockSeatIdList.addAll(v.stream().map(SeatVo::getId).toList());
        });

        List<String> keys = new ArrayList<>();
        //操作类型
        keys.add(String.valueOf(orderStatus.getCode()));

        Object[] data = new String[3];
        //扣除锁定的座位数据
        data[0] = JSON.toJSONString(unLockSeatIdjsonArray);
        //如果是订单取消的操作，那么添加到未售卖的座位数据
        //如果是订单支付的操作，那么添加到已售卖的座位数据
        data[1] = JSON.toJSONString(addSeatDatajsonArray);
        //恢复库存数据
        data[2] = JSON.toJSONString(jsonArray);
        //执行lua脚本
        orderProgramCacheResolutionOperate.programCacheReverseOperate(keys, data);

        if (Objects.equals(orderStatus.getCode(), com.ticketflow.enums.OrderStatus.PAY.getCode())) {
            ProgramOperateDataDto programOperateDataDto = new ProgramOperateDataDto();
            programOperateDataDto.setProgramId(programId);
            programOperateDataDto.setSeatIdList(unLockSeatIdList);
            programOperateDataDto.setTicketCategoryCountDtoList(ticketCategoryCountDtoList);
            programOperateDataDto.setSellStatus(SellStatus.SOLD.getCode());
            delayOperateProgramDataSend.sendMessage(JSON.toJSONString(programOperateDataDto));
        }
    }

    public String pay(OrderPayDto orderPayDto) {
        Long orderNumber = orderPayDto.getOrderNumber();
        LambdaQueryWrapper<Order> orderLambdaQueryWrapper =
                Wrappers.lambdaQuery(Order.class).eq(Order::getOrderNumber, orderNumber);
        Order order = orderMapper.selectOne(orderLambdaQueryWrapper);
        if (Objects.isNull(order)) {
            throw new TicketFlowFrameException(BaseCode.ORDER_NOT_EXIST);
        }
        if (Objects.equals(order.getOrderStatus(), OrderStatus.CANCEL.getCode())) {
            throw new TicketFlowFrameException(BaseCode.ORDER_CANCEL);
        }
        if (Objects.equals(order.getOrderStatus(), OrderStatus.PAY.getCode())) {
            throw new TicketFlowFrameException(BaseCode.ORDER_PAY);
        }
        if (Objects.equals(order.getOrderStatus(), OrderStatus.REFUND.getCode())) {
            throw new TicketFlowFrameException(BaseCode.ORDER_REFUND);
        }
        if (orderPayDto.getPrice().compareTo(order.getOrderPrice()) != 0) {
            throw new TicketFlowFrameException(BaseCode.PAY_PRICE_NOT_EQUAL_ORDER_PRICE);
        }
        PayDto payDto = getPayDto(orderPayDto, orderNumber);
        ApiResponse<String> payResponse = payClient.commonPay(payDto);
        if (!Objects.equals(payResponse.getCode(), BaseCode.SUCCESS.getCode())) {
            throw new TicketFlowFrameException(payResponse);
        }
        return payResponse.getData();
    }

    private PayDto getPayDto(OrderPayDto orderPayDto, Long orderNumber) {
        PayDto payDto = new PayDto();
        payDto.setOrderNumber(String.valueOf(orderNumber));
        payDto.setPayBillType(orderPayDto.getPayBillType());
        payDto.setSubject(orderPayDto.getSubject());
        payDto.setChannel(orderPayDto.getChannel());
        payDto.setPlatform(orderPayDto.getPlatform());
        payDto.setPrice(orderPayDto.getPrice());
        payDto.setNotifyUrl(orderProperties.getOrderPayNotifyUrl());
        payDto.setReturnUrl(orderProperties.getOrderPayReturnUrl());
        return payDto;
    }

    @ServiceLock(name = ORDER_PAY_CHECK, keys = {"#orderPayCheckDto.orderNumber"})
    public OrderPayCheckVo payCheck(OrderPayCheckDto orderPayCheckDto) {
        OrderPayCheckVo orderPayCheckVo = new OrderPayCheckVo();
        LambdaQueryWrapper<Order> orderLambdaQueryWrapper =
                Wrappers.lambdaQuery(Order.class).eq(Order::getOrderNumber,
                        orderPayCheckDto.getOrderNumber());
        Order order = orderMapper.selectOne(orderLambdaQueryWrapper);
        if (Objects.isNull(order)) {
            throw new com.ticketflow.exception.TicketFlowFrameException(BaseCode.ORDER_NOT_EXIST);
        }
        BeanUtil.copyProperties(order, orderPayCheckVo);

        //如果订单已取消则进行退款
        if (Objects.equals(order.getOrderStatus(), OrderStatus.CANCEL.getCode())) {
            RefundDto refundDto = new RefundDto();
            refundDto.setOrderNumber(String.valueOf(order.getOrderNumber()));
            refundDto.setAmount(order.getOrderPrice());
            refundDto.setChannel("alipay");
            refundDto.setReason("延迟订单关闭");
            ApiResponse<String> response = payClient.refund(refundDto);
            if (response.getCode().equals(BaseCode.SUCCESS.getCode())) {
                //调用支付服务退款成功后，把订单更新为已退款状态
                Order updateOrder = new Order();
                updateOrder.setEditTime(com.damai.util.DateUtils.now());
                updateOrder.setOrderStatus(OrderStatus.REFUND.getCode());
                orderMapper.update(updateOrder, Wrappers.lambdaUpdate(Order.class).
                        eq(Order::getOrderNumber, order.getOrderNumber()));
            } else {
                log.error("pay服务退款失败 dto : {} response : {}",
                        JSON.toJSONString(refundDto), JSON.toJSONString(response));
            }
            orderPayCheckVo.setOrderStatus(OrderStatus.REFUND.getCode());
            orderPayCheckVo.setCancelOrderTime(com.damai.util.DateUtils.now());
            return orderPayCheckVo;
        }

        //调用支付服务查询支付渠道的真实状态
        TradeCheckDto tradeCheckDto = new TradeCheckDto();
        tradeCheckDto.setOutTradeNo(String.valueOf(orderPayCheckDto.getOrderNumber()));
        tradeCheckDto.setChannel(Optional.ofNullable(PayChannel.getRc(orderPayCheckDto.getPayChannelType()))
                .map(PayChannel::getValue).orElseThrow(() ->
                        new TicketFlowFrameException(BaseCode.PAY_CHANNEL_NOT_EXIST)));
        // RPC查看真实状态
        ApiResponse<TradeCheckVo> tradeCheckVoApiResponse = payClient.tradeCheck(tradeCheckDto);
        if (!Objects.equals(tradeCheckVoApiResponse.getCode(), BaseCode.SUCCESS.getCode())) {
            throw new TicketFlowFrameException(tradeCheckVoApiResponse);
        }
        TradeCheckVo tradeCheckVo = Optional.ofNullable(tradeCheckVoApiResponse.getData())
                .orElseThrow(() -> new TicketFlowFrameException(BaseCode.PAY_BILL_NOT_EXIST));
        if (tradeCheckVo.isSuccess()) {
            Integer payBillStatus = tradeCheckVo.getPayBillStatus();
            Integer orderStatus = order.getOrderStatus();
            //如果订单的状态和账单的状态不一致，说明支付的回调没有成功，那么就在这里更新数据
            if (!Objects.equals(orderStatus, payBillStatus)) {
                orderPayCheckVo.setOrderStatus(payBillStatus);
                try {
                    //如果账单的状态是支付，那么执行订单支付的操作
                    if (Objects.equals(payBillStatus, PayBillStatus.PAY.getCode())) {
                        orderPayCheckVo.setPayOrderTime(com.damai.util.DateUtils.now());
                        orderService.updateOrderRelatedData(order.getOrderNumber(), OrderStatus.PAY);
                        //如果账单的状态是取消，那么执行订单取消的操作
                    } else if (Objects.equals(payBillStatus, PayBillStatus.CANCEL.getCode())) {
                        orderPayCheckVo.setCancelOrderTime(com.damai.util.DateUtils.now());
                        orderService.updateOrderRelatedData(order.getOrderNumber(), OrderStatus.CANCEL);
                    }
                } catch (Exception e) {
                    log.warn("updateOrderRelatedData warn message", e);
                }
            }
        } else {
            throw new TicketFlowFrameException(BaseCode.PAY_TRADE_CHECK_ERROR);
        }
        return orderPayCheckVo;
    }

    public List<OrderListVo> selectList(OrderListDto orderListDto) {
        List<OrderListVo> orderListVos = new ArrayList<>();
        LambdaQueryWrapper<Order> orderLambdaQueryWrapper =
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getUserId, orderListDto.getUserId())
                        .orderByDesc(Order::getCreateOrderTime);
        List<Order> orderList = orderMapper.selectList(orderLambdaQueryWrapper);
        if (CollectionUtil.isEmpty(orderList)) {
            return orderListVos;
        }
        orderListVos = BeanUtil.copyToList(orderList, OrderListVo.class);
        List<OrderTicketUserAggregate> orderTicketUserAggregateList =
                orderTicketUserMapper.selectOrderTicketUserAggregate(orderList.stream().map(Order::getOrderNumber).
                        collect(Collectors.toList()));
        Map<Long, Integer> orderTicketUserAggregateMap = orderTicketUserAggregateList.stream()
                .collect(Collectors.toMap(OrderTicketUserAggregate::getOrderNumber,
                        OrderTicketUserAggregate::getOrderTicketUserCount, (v1, v2) -> v2));
        for (OrderListVo orderListVo : orderListVos) {
            orderListVo.setTicketCount(orderTicketUserAggregateMap.get(orderListVo.getOrderNumber()));
        }
        return orderListVos;
    }

    public OrderGetVo get(OrderGetDto orderGetDto) {
        //查询订单
        LambdaQueryWrapper<Order> orderLambdaQueryWrapper =
                Wrappers.lambdaQuery(Order.class).eq(Order::getOrderNumber, orderGetDto.getOrderNumber());
        Order order = orderMapper.selectOne(orderLambdaQueryWrapper);
        if (Objects.isNull(order)) {
            throw new TicketFlowFrameException(BaseCode.ORDER_NOT_EXIST);
        }
        //查询购票人订单
        LambdaQueryWrapper<OrderTicketUser> orderTicketUserLambdaQueryWrapper =
                Wrappers.lambdaQuery(OrderTicketUser.class).eq(OrderTicketUser::getOrderNumber, order.getOrderNumber());
        List<OrderTicketUser> orderTicketUserList = orderTicketUserMapper.selectList(orderTicketUserLambdaQueryWrapper);
        if (CollectionUtil.isEmpty(orderTicketUserList)) {
            throw new TicketFlowFrameException(BaseCode.TICKET_USER_ORDER_NOT_EXIST);
        }

        OrderGetVo orderGetVo = new OrderGetVo();
        BeanUtil.copyProperties(order, orderGetVo);

        //组装购票订单信息
        List<OrderTicketInfoVo> orderTicketInfoVoList = new ArrayList<>();
        //按照购票订单的金额进行分组
        Map<BigDecimal, List<OrderTicketUser>> orderTicketUserMap =
                orderTicketUserList.stream().collect(Collectors.groupingBy(OrderTicketUser::getOrderPrice));
        orderTicketUserMap.forEach((k, v) -> {
            OrderTicketInfoVo orderTicketInfoVo = new OrderTicketInfoVo();
            String seatInfo = "暂无座位信息";
            //如果节目是允许选座的，才显示出当时生成订单时产生的座位信息
            if (order.getProgramPermitChooseSeat().equals(BusinessStatus.YES.getCode())) {
                seatInfo = v.stream().map(OrderTicketUser::getSeatInfo).collect(Collectors.joining(","));
            }
            orderTicketInfoVo.setSeatInfo(seatInfo);
            orderTicketInfoVo.setPrice(v.get(0).getOrderPrice());
            orderTicketInfoVo.setQuantity(v.size());
            orderTicketInfoVo.setRelPrice(v.stream().map(OrderTicketUser::getOrderPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            orderTicketInfoVoList.add(orderTicketInfoVo);
        });

        orderGetVo.setOrderTicketInfoVoList(orderTicketInfoVoList);

        //查询用户和购票人信息
        UserGetAndTicketUserListDto userGetAndTicketUserListDto = new UserGetAndTicketUserListDto();
        userGetAndTicketUserListDto.setUserId(order.getUserId());
        ApiResponse<UserGetAndTicketUserListVo> userGetAndTicketUserApiResponse =
                userClient.getUserAndTicketUserList(userGetAndTicketUserListDto);

        if (!Objects.equals(userGetAndTicketUserApiResponse.getCode(), BaseCode.SUCCESS.getCode())) {
            throw new TicketFlowFrameException(userGetAndTicketUserApiResponse);

        }
        //验证用户和购票人信息是否存在
        UserGetAndTicketUserListVo userAndTicketUserListVo =
                Optional.ofNullable(userGetAndTicketUserApiResponse.getData())
                        .orElseThrow(() -> new com.ticketflow.exception.TicketFlowFrameException(BaseCode.RPC_RESULT_DATA_EMPTY));
        //如果用户信息空，抛出异常
        if (Objects.isNull(userAndTicketUserListVo.getUserVo())) {
            throw new TicketFlowFrameException(BaseCode.USER_EMPTY);
        }
        //如果购票人信息空，抛出异常
        if (CollectionUtil.isEmpty(userAndTicketUserListVo.getTicketUserVoList())) {
            throw new TicketFlowFrameException(BaseCode.TICKET_USER_EMPTY);
        }
        //从查询得到的购票人信息中进行过滤出该订单下购票人的信息
        List<TicketUserVo> filterTicketUserVoList = new ArrayList<>();
        Map<Long, TicketUserVo> ticketUserVoMap = userAndTicketUserListVo.getTicketUserVoList()
                .stream().collect(Collectors.toMap(TicketUserVo::getId, ticketUserVo -> ticketUserVo, (v1, v2) -> v2));
        for (OrderTicketUser orderTicketUser : orderTicketUserList) {
            filterTicketUserVoList.add(ticketUserVoMap.get(orderTicketUser.getTicketUserId()));
        }
        //组装数据
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtil.copyProperties(userAndTicketUserListVo.getUserVo(), userInfoVo);
        UserAndTicketUserInfoVo userAndTicketUserInfoVo = new UserAndTicketUserInfoVo();
        userAndTicketUserInfoVo.setUserInfoVo(userInfoVo);
        userAndTicketUserInfoVo.setTicketUserInfoVoList(BeanUtil.copyToList(filterTicketUserVoList, TicketUserInfoVo.class));
        orderGetVo.setUserAndTicketUserInfoVo(userAndTicketUserInfoVo);

        return orderGetVo;
    }

    public String alipayNotify(HttpServletRequest request) {
        // 把回调的参数转为Map结构
        Map<String, String> params = new HashMap<>(256);
        if (request instanceof final CustomizeRequestWrapper customizeRequestWrapper) {
            String requestBody = customizeRequestWrapper.getRequestBody();
            params = StringUtil.convertQueryStringToMap(requestBody);
        }
        log.info("收到支付宝回调通知 params : {}", JSON.toJSONString(params));
        // 获取其中的订单号
        String outTradeNo = params.get("out_trade_no");
        if (StringUtil.isEmpty(outTradeNo)) {
            return "failure";
        }
        // 加锁
        RLock lock = serviceLockTool.getLock(LockType.Reentrant, ORDER_PAY_NOTIFY_CHECK,
                new String[]{outTradeNo});
        lock.lock();
        try {
            Order order = orderMapper.selectOne(Wrappers.lambdaQuery(Order.class).eq(Order::getOrderNumber, Long.parseLong(outTradeNo)));
            if (Objects.isNull(order)) {
                throw new TicketFlowFrameException(BaseCode.ORDER_NOT_EXIST);
            }
            // 订单已取消，则进行退款
            if (Objects.equals(order.getOrderStatus(), OrderStatus.CANCEL.getCode())) {
                RefundDto refundDto = new RefundDto();
                refundDto.setOrderNumber(outTradeNo);
                refundDto.setAmount(order.getOrderPrice());
                refundDto.setChannel("alipay");
                refundDto.setReason("延迟订单关闭");
                // 调用退款接口进行退款
                ApiResponse<String> response = payClient.refund(refundDto);
                if (response.getCode().equals(BaseCode.SUCCESS.getCode())) {
                    Order updateOrder = new Order();
                    updateOrder.setEditTime(com.damai.util.DateUtils.now());
                    updateOrder.setOrderStatus(OrderStatus.REFUND.getCode());
                    orderMapper.update(updateOrder, Wrappers.lambdaUpdate(Order.class).eq(Order::getOrderNumber, outTradeNo));
                } else {
                    log.error("pay服务退款失败 dto : {} response : {}", JSON.toJSONString(refundDto), JSON.toJSONString(response));
                }
                return ALIPAY_NOTIFY_SUCCESS_RESULT;
            }


            NotifyDto notifyDto = new NotifyDto();
            notifyDto.setChannel(PayChannel.ALIPAY.getValue());
            notifyDto.setParams(params);
            // 对回调的参数进行签名验证
            ApiResponse<NotifyVo> notifyResponse = payClient.notify(notifyDto);
            if (!Objects.equals(notifyResponse.getCode(), BaseCode.SUCCESS.getCode())) {
                throw new TicketFlowFrameException(notifyResponse);
            }
            // 验证成功，进行订单状态更新
            if (ALIPAY_NOTIFY_SUCCESS_RESULT.equals(notifyResponse.getData().getPayResult())) {
                try {
                    orderService.updateOrderRelatedData(Long.parseLong(notifyResponse.getData().getOutTradeNo())
                            , OrderStatus.PAY);
                } catch (Exception e) {
                    log.warn("updateOrderRelatedData warn message", e);
                }
            }
            return notifyResponse.getData().getPayResult();
        } finally {
            lock.unlock();
        }

    }

    public AccountOrderCountVo accountOrderCount(AccountOrderCountDto accountOrderCountDto) {
        AccountOrderCountVo accountOrderCountVo = new AccountOrderCountVo();
        accountOrderCountVo.setCount(orderMapper.accountOrderCount(accountOrderCountDto.getUserId(),
                accountOrderCountDto.getProgramId()));
        return accountOrderCountVo;
    }


    @RepeatExecuteLimit(name = CREATE_PROGRAM_ORDER_MQ, keys = {"#orderCreateDto.orderNumber"})
    @Transactional(rollbackFor = Exception.class)
    public String createMq(OrderCreateDto orderCreateDto) {
        String orderNumber = create(orderCreateDto);
        redisCache.set(RedisKeyBuild.createRedisKey(RedisKeyManage.ORDER_MQ, orderNumber), orderNumber, 1, TimeUnit.MINUTES);
        return orderNumber;
    }

    @RepeatExecuteLimit(name = PROGRAM_CACHE_REVERSE_MQ, keys = {"#programId"})
    public void updateProgramRelatedDataMq(Long programId, Map<Long, List<Long>> seatMap, OrderStatus orderStatus) {
        updateProgramRelatedDataResolution(programId, seatMap, orderStatus);
    }

    public String getCache(OrderGetDto orderGetDto) {
        return redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.ORDER_MQ, orderGetDto.getOrderNumber()), String.class);
    }

    @RepeatExecuteLimit(name = CANCEL_PROGRAM_ORDER, keys = {"#orderCancelDto.orderNumber"})
    @ServiceLock(name = ORDER_CANCEL_LOCK, keys = {"#orderCancelDto.orderNumber"})
    @Transactional(rollbackFor = Exception.class)
    public boolean initiateCancel(OrderCancelDto orderCancelDto) {
        Order order = orderMapper.selectOne(Wrappers.lambdaQuery(Order.class)
                .eq(Order::getOrderNumber, orderCancelDto.getOrderNumber()));
        if (Objects.isNull(order)) {
            throw new TicketFlowFrameException(BaseCode.ORDER_NOT_EXIST);
        }
        if (!Objects.equals(order.getOrderStatus(), OrderStatus.NO_PAY.getCode())) {
            throw new TicketFlowFrameException(BaseCode.CAN_NOT_CANCEL);
        }
        return cancel(orderCancelDto);
    }

}
