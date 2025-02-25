package com.ticketflow.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.fsg.uid.UidGenerator;
import com.ticketflow.client.OrderClient;
import com.ticketflow.common.ApiResponse;
import com.ticketflow.core.RedisKeyManage;
import com.ticketflow.dto.*;
import com.ticketflow.entity.ProgramShowTime;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.enums.OrderStatus;
import com.ticketflow.enums.SellStatus;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.redis.RedisKeyBuild;
import com.ticketflow.service.delaysend.DelayOrderCancelSend;
import com.ticketflow.service.kafka.CreateOrderMqDomain;
import com.ticketflow.service.kafka.CreateOrderSend;
import com.ticketflow.service.lua.ProgramCacheCreateOrderData;
import com.ticketflow.service.lua.ProgramCacheCreateOrderResolutionOperate;
import com.ticketflow.service.lua.ProgramCacheResolutionOperate;
import com.ticketflow.service.tool.SeatMatch;
import com.ticketflow.utils.DateUtils;
import com.ticketflow.vo.ProgramVo;
import com.ticketflow.vo.SeatVo;
import com.ticketflow.vo.TicketCategoryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ticketflow.service.constant.ProgramOrderConstant.ORDER_TABLE_COUNT;

/**
 * @Description: 节目订单 service
 * @Author: rickey-c
 * @Date: 2025/2/3 20:55
 */
@Slf4j
@Service
public class ProgramOrderService {

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private UidGenerator uidGenerator;

    @Autowired
    private ProgramCacheResolutionOperate programCacheResolutionOperate;

    @Autowired
    ProgramCacheCreateOrderResolutionOperate programCacheCreateOrderResolutionOperate;

    @Autowired
    private DelayOrderCancelSend delayOrderCancelSend;

    @Autowired
    private CreateOrderSend createOrderSend;

    @Autowired
    private ProgramService programService;

    @Autowired
    private ProgramShowTimeService programShowTimeService;

    @Autowired
    private TicketCategoryService ticketCategoryService;

    @Autowired
    private SeatService seatService;

    public List<TicketCategoryVo> getTicketCategoryList(ProgramOrderCreateDto programOrderCreateDto, Date showTime) {
        // 返回查询到的票档集合
        List<TicketCategoryVo> getTicketCategoryVoList = new ArrayList<>();
        // 从缓存中查到所有票档集合
        List<TicketCategoryVo> ticketCategoryVoList = ticketCategoryService.selectTicketCategoryListByProgramIdMultipleCache(programOrderCreateDto.getProgramId(), showTime);
        // 把所有票档集合转成Map，方便后续进行操作
        Map<Long, TicketCategoryVo> ticketCategoryVoMap = ticketCategoryVoList.stream().collect(Collectors.toMap(TicketCategoryVo::getId, ticketCategoryVo -> ticketCategoryVo));
        // 从订单参数中获取座位
        List<SeatDto> seatDtoList = programOrderCreateDto.getSeatDtoList();
        // 针对手动选座，传入的是seatDtoList
        if (CollectionUtil.isNotEmpty(seatDtoList)) {
            // 遍历选的座位
            for (SeatDto seatDto : seatDtoList) {
                // 通过座位的票档id和票档map的票档id进行比较
                TicketCategoryVo ticketCategoryVo = ticketCategoryVoMap.get(seatDto.getTicketCategoryId());
                if (Objects.nonNull(ticketCategoryVo)) {
                    getTicketCategoryVoList.add(ticketCategoryVo);
                } else {
                    throw new TicketFlowFrameException(BaseCode.TICKET_CATEGORY_NOT_EXIST_V2);
                }
            }
            // 针对自动选座，传入的是ticketCategoryVo
        } else {
            // 自动选座没有座位，因此直接拿票档id进行比较
            TicketCategoryVo ticketCategoryVo = ticketCategoryVoMap.get(programOrderCreateDto.getTicketCategoryId());
            if (Objects.nonNull(ticketCategoryVo)) {
                getTicketCategoryVoList.add(ticketCategoryVo);
            } else {
                throw new TicketFlowFrameException(BaseCode.TICKET_CATEGORY_NOT_EXIST_V2);
            }
        }
        return getTicketCategoryVoList;
    }

    public String create(ProgramOrderCreateDto programOrderCreateDto) {
        // 演出时间
        ProgramShowTime programShowTime = programShowTimeService.selectProgramShowTimeByProgramIdMultipleCache(programOrderCreateDto.getProgramId());
        // 获取节目票档(需要配合时间来保证时效性)
        List<TicketCategoryVo> getTicketCategoryList = getTicketCategoryList(programOrderCreateDto, programShowTime.getShowTime());
        BigDecimal parameterOrderPrice = new BigDecimal("0");
        BigDecimal databaseOrderPrice = new BigDecimal("0");
        List<SeatVo> purchaseSeatList = new ArrayList<>();
        // 获取座位集合
        List<SeatDto> seatDtoList = programOrderCreateDto.getSeatDtoList();
        List<SeatVo> seatVoList = new ArrayList<>();
        Map<String, Long> ticketCategoryRemainNumber = new HashMap<>(16);
        // 遍历票档
        for (TicketCategoryVo ticketCategory : getTicketCategoryList) {
            // 通过节目id+票档id查询对应的座位
            List<SeatVo> allSeatVoList = seatService.selectSeatResolution(programOrderCreateDto.getProgramId(), ticketCategory.getId(), DateUtils.countBetweenSecond(DateUtils.now(), programShowTime.getShowTime()), TimeUnit.SECONDS);
            // 构建要扣减的座位
            seatVoList.addAll(allSeatVoList.stream().filter(seatVo -> seatVo.getSellStatus().equals(SellStatus.NO_SOLD.getCode())).toList());
            // 构建余票数量
            ticketCategoryRemainNumber.putAll(ticketCategoryService.getRedisRemainNumberResolution(programOrderCreateDto.getProgramId(), ticketCategory.getId()));
        }
        // 手动选座
        if (CollectionUtil.isNotEmpty(seatDtoList)) {
            // 转成map，方便后续处理
            Map<Long, Long> seatTicketCategoryDtoCount = seatDtoList.stream().collect(Collectors.groupingBy(SeatDto::getTicketCategoryId, Collectors.counting()));
            // 检查每个票档的余票数量是否还够
            for (Entry<Long, Long> entry : seatTicketCategoryDtoCount.entrySet()) {
                Long ticketCategoryId = entry.getKey();
                Long purchaseCount = entry.getValue();
                Long remainNumber = Optional.ofNullable(ticketCategoryRemainNumber.get(String.valueOf(ticketCategoryId))).orElseThrow(() -> new TicketFlowFrameException(BaseCode.TICKET_CATEGORY_NOT_EXIST_V2));
                // 购买数量大于余票数量，抛出异常
                if (purchaseCount > remainNumber) {
                    throw new TicketFlowFrameException(BaseCode.TICKET_REMAIN_NUMBER_NOT_SUFFICIENT);
                }
            }
            // 遍历座位
            for (SeatDto seatDto : seatDtoList) {
                // 构建<行号-列号,seatVo>的map
                Map<String, SeatVo> seatVoMap = seatVoList.stream().collect(Collectors.toMap(seat -> seat.getRowCode() + "-" + seat.getColCode(), seat -> seat, (v1, v2) -> v2));
                SeatVo seatVo = seatVoMap.get(seatDto.getRowCode() + "-" + seatDto.getColCode());
                if (Objects.isNull(seatVo)) {
                    throw new TicketFlowFrameException(BaseCode.SEAT_IS_NOT_NOT_SOLD);
                }
                // 构建订单参数
                purchaseSeatList.add(seatVo);
                parameterOrderPrice = parameterOrderPrice.add(seatDto.getPrice());
                databaseOrderPrice = databaseOrderPrice.add(seatVo.getPrice());
            }
            // 总价格校验
            if (parameterOrderPrice.compareTo(databaseOrderPrice) > 0) {
                throw new TicketFlowFrameException(BaseCode.PRICE_ERROR);
            }
            // 自动选座
        } else {
            Long ticketCategoryId = programOrderCreateDto.getTicketCategoryId();
            Integer ticketCount = programOrderCreateDto.getTicketCount();
            Long remainNumber = Optional.ofNullable(ticketCategoryRemainNumber.get(String.valueOf(ticketCategoryId))).orElseThrow(() -> new TicketFlowFrameException(BaseCode.TICKET_CATEGORY_NOT_EXIST_V2));
            // 余票数量校验
            if (ticketCount > remainNumber) {
                throw new TicketFlowFrameException(BaseCode.TICKET_REMAIN_NUMBER_NOT_SUFFICIENT);
            }
            // 算法检索座位
            purchaseSeatList = SeatMatch.findAdjacentSeatVos(seatVoList.stream().filter(seatVo -> Objects.equals(seatVo.getTicketCategoryId(), ticketCategoryId)).collect(Collectors.toList()), ticketCount);
            if (purchaseSeatList.size() < ticketCount) {
                throw new TicketFlowFrameException(BaseCode.SEAT_OCCUPY);
            }
        }
        // 更新缓存数据
        updateProgramCacheDataResolution(programOrderCreateDto.getProgramId(), purchaseSeatList, OrderStatus.NO_PAY);
        return doCreate(programOrderCreateDto, purchaseSeatList);
    }


    public String createNew(ProgramOrderCreateDto programOrderCreateDto) {
        List<SeatVo> purchaseSeatList = createOrderOperateProgramCacheResolution(programOrderCreateDto);
        return doCreate(programOrderCreateDto, purchaseSeatList);
    }

    public String createNewAsync(ProgramOrderCreateDto programOrderCreateDto) {
        List<SeatVo> purchaseSeatList = createOrderOperateProgramCacheResolution(programOrderCreateDto);
        return doCreateV2(programOrderCreateDto, purchaseSeatList);
    }

    public List<SeatVo> createOrderOperateProgramCacheResolution(ProgramOrderCreateDto programOrderCreateDto) {
        // 查询演出时间
        ProgramShowTime programShowTime = programShowTimeService.selectProgramShowTimeByProgramIdMultipleCache(programOrderCreateDto.getProgramId());
        // 查询票档集合
        List<TicketCategoryVo> getTicketCategoryList = getTicketCategoryList(programOrderCreateDto, programShowTime.getShowTime());
        // 遍历票档
        for (TicketCategoryVo ticketCategory : getTicketCategoryList) {
            // 缓存预热保险，方便后续进行查询，同时使用双重检测，保证并发度
            // 从缓存中查询座位，如果缓存不存在，则从数据库查询后再放入缓存
            seatService.selectSeatResolution(programOrderCreateDto.getProgramId(), ticketCategory.getId(), DateUtils.countBetweenSecond(DateUtils.now(), programShowTime.getShowTime()), TimeUnit.SECONDS);
            // 从缓存中查询余票数量，如果缓存不存在，则从数据库查询后再放入缓存
            ticketCategoryService.getRedisRemainNumberResolution(programOrderCreateDto.getProgramId(), ticketCategory.getId());
        }
        Long programId = programOrderCreateDto.getProgramId();
        // 获取选的座位
        List<SeatDto> seatDtoList = programOrderCreateDto.getSeatDtoList();
        // keys主要是区分手动和自动选座
        List<String> keys = new ArrayList<>();
        // 传入的数据
        String[] data = new String[2];
        // 更新票档数据集合
        JSONArray ticketJsonArray = new JSONArray();
        // 添加座位集合
        JSONArray addSeatDatajsonArray = new JSONArray();
        if (CollectionUtil.isNotEmpty(seatDtoList)) {
            // 手动选座
            keys.add("1");
            // 通过票档对座位进行分组
            Map<Long, List<SeatDto>> seatTicketCategoryDtoCount = seatDtoList.stream().collect(Collectors.groupingBy(SeatDto::getTicketCategoryId));
            for (Entry<Long, List<SeatDto>> entry : seatTicketCategoryDtoCount.entrySet()) {
                // 票档id
                Long ticketCategoryId = entry.getKey();
                // 对应票档的购买数量
                int ticketCount = entry.getValue().size();
                // 拼接参数
                JSONObject jsonObject = new JSONObject();
                // 票档id的key
                jsonObject.put("programTicketRemainNumberHashKey", RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_TICKET_REMAIN_NUMBER_HASH_RESOLUTION, programId, ticketCategoryId).getRelKey());
                // 票档id
                jsonObject.put("ticketCategoryId", ticketCategoryId);
                // 买票数量
                jsonObject.put("ticketCount", ticketCount);
                ticketJsonArray.add(jsonObject);

                JSONObject seatDatajsonObject = new JSONObject();
                // 未售卖座位hash的key
                seatDatajsonObject.put("seatNoSoldHashKey", RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_NO_SOLD_RESOLUTION_HASH, programId, ticketCategoryId).getRelKey());
                // 要购买的座位数据
                seatDatajsonObject.put("seatDataList", JSON.toJSONString(seatDtoList));
                addSeatDatajsonArray.add(seatDatajsonObject);
            }
        } else {
            // 自动选座
            keys.add("2");
            Long ticketCategoryId = programOrderCreateDto.getTicketCategoryId();
            Integer ticketCount = programOrderCreateDto.getTicketCount();
            JSONObject jsonObject = new JSONObject();
            // 自动选座只能选一种票档，直接拼接就可以
            jsonObject.put("programTicketRemainNumberHashKey", RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_TICKET_REMAIN_NUMBER_HASH_RESOLUTION, programId, ticketCategoryId).getRelKey());
            jsonObject.put("ticketCategoryId", ticketCategoryId);
            jsonObject.put("ticketCount", ticketCount);
            jsonObject.put("seatNoSoldHashKey", RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_NO_SOLD_RESOLUTION_HASH, programId, ticketCategoryId).getRelKey());
            ticketJsonArray.add(jsonObject);
        }
        // 未售卖座位hash的key
        keys.add(RedisKeyBuild.getRedisKey(RedisKeyManage.PROGRAM_SEAT_NO_SOLD_RESOLUTION_HASH));
        // 锁定座位hash的key
        keys.add(RedisKeyBuild.getRedisKey(RedisKeyManage.PROGRAM_SEAT_LOCK_RESOLUTION_HASH));
        // 节目id
        keys.add(String.valueOf(programOrderCreateDto.getProgramId()));
        // 购票参数
        data[0] = JSON.toJSONString(ticketJsonArray);
        // 座位参数
        data[1] = JSON.toJSONString(addSeatDatajsonArray);
        // 执行lua脚本，keys主要是存放redis的键，data存放要修改的数据
        ProgramCacheCreateOrderData programCacheCreateOrderData = programCacheCreateOrderResolutionOperate.programCacheOperate(keys, data);
        if (!Objects.equals(programCacheCreateOrderData.getCode(), BaseCode.SUCCESS.getCode())) {
            throw new TicketFlowFrameException(Objects.requireNonNull(BaseCode.getRc(programCacheCreateOrderData.getCode())));
        }
        return programCacheCreateOrderData.getPurchaseSeatList();
    }

    private String doCreate(ProgramOrderCreateDto programOrderCreateDto, List<SeatVo> purchaseSeatList) {
        OrderCreateDto orderCreateDto = buildCreateOrderParam(programOrderCreateDto, purchaseSeatList);
        // 通过RPC创建订单
        String orderNumber = createOrderByRpc(orderCreateDto, purchaseSeatList);

        // 订单加入延迟队列，如果超时就取消
        DelayOrderCancelDto delayOrderCancelDto = new DelayOrderCancelDto();
        delayOrderCancelDto.setOrderNumber(orderCreateDto.getOrderNumber());
        delayOrderCancelSend.sendMessage(JSON.toJSONString(delayOrderCancelDto));

        // 返回订单编号
        return orderNumber;
    }

    private String doCreateV2(ProgramOrderCreateDto programOrderCreateDto, List<SeatVo> purchaseSeatList) {
        OrderCreateDto orderCreateDto = buildCreateOrderParam(programOrderCreateDto, purchaseSeatList);

        String orderNumber = createOrderByMq(orderCreateDto, purchaseSeatList);

        // 发送延时消息用以取消超时订单
        DelayOrderCancelDto delayOrderCancelDto = new DelayOrderCancelDto();
        delayOrderCancelDto.setOrderNumber(orderCreateDto.getOrderNumber());
        delayOrderCancelSend.sendMessage(JSON.toJSONString(delayOrderCancelDto));

        return orderNumber;
    }

    private OrderCreateDto buildCreateOrderParam(ProgramOrderCreateDto programOrderCreateDto, List<SeatVo> purchaseSeatList) {
        // 获取节目信息
        ProgramVo programVo = programService.simpleGetProgramAndShowMultipleCache(programOrderCreateDto.getProgramId());
        OrderCreateDto orderCreateDto = new OrderCreateDto();
        // 构建主订单参数
        // 基因法构建订单ID
        orderCreateDto.setOrderNumber(uidGenerator.getOrderNumber(programOrderCreateDto.getUserId(), ORDER_TABLE_COUNT));
        orderCreateDto.setProgramId(programOrderCreateDto.getProgramId());
        orderCreateDto.setProgramItemPicture(programVo.getItemPicture());
        orderCreateDto.setUserId(programOrderCreateDto.getUserId());
        orderCreateDto.setProgramTitle(programVo.getTitle());
        orderCreateDto.setProgramPlace(programVo.getPlace());
        orderCreateDto.setProgramShowTime(programVo.getShowTime());
        orderCreateDto.setProgramPermitChooseSeat(programVo.getPermitChooseSeat());
        BigDecimal databaseOrderPrice = purchaseSeatList.stream().map(SeatVo::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        orderCreateDto.setOrderPrice(databaseOrderPrice);
        orderCreateDto.setCreateOrderTime(DateUtils.now());

        // 购票人订单创建，相当于是订单项
        List<Long> ticketUserIdList = programOrderCreateDto.getTicketUserIdList();
        List<OrderTicketUserCreateDto> orderTicketUserCreateDtoList = new ArrayList<>();
        for (int i = 0; i < ticketUserIdList.size(); i++) {
            Long ticketUserId = ticketUserIdList.get(i);
            OrderTicketUserCreateDto orderTicketUserCreateDto = new OrderTicketUserCreateDto();
            orderTicketUserCreateDto.setOrderNumber(orderCreateDto.getOrderNumber());
            orderTicketUserCreateDto.setProgramId(programOrderCreateDto.getProgramId());
            orderTicketUserCreateDto.setUserId(programOrderCreateDto.getUserId());
            orderTicketUserCreateDto.setTicketUserId(ticketUserId);
            // 给购票人绑定座位
            SeatVo seatVo = Optional.ofNullable(purchaseSeatList.get(i)).orElseThrow(() -> new TicketFlowFrameException(BaseCode.SEAT_NOT_EXIST));
            orderTicketUserCreateDto.setSeatId(seatVo.getId());
            orderTicketUserCreateDto.setSeatInfo(seatVo.getRowCode() + "排" + seatVo.getColCode() + "列");
            orderTicketUserCreateDto.setTicketCategoryId(seatVo.getTicketCategoryId());
            orderTicketUserCreateDto.setOrderPrice(seatVo.getPrice());
            orderTicketUserCreateDto.setCreateOrderTime(DateUtils.now());
            orderTicketUserCreateDtoList.add(orderTicketUserCreateDto);
        }
        orderCreateDto.setOrderTicketUserCreateDtoList(orderTicketUserCreateDtoList);

        return orderCreateDto;
    }

    private String createOrderByRpc(OrderCreateDto orderCreateDto, List<SeatVo> purchaseSeatList) {
        ApiResponse<String> createOrderResponse = orderClient.create(orderCreateDto);
        if (!Objects.equals(createOrderResponse.getCode(), BaseCode.SUCCESS.getCode())) {
            log.error("创建订单失败 需人工处理 orderCreateDto : {}", JSON.toJSONString(orderCreateDto));
            updateProgramCacheDataResolution(orderCreateDto.getProgramId(), purchaseSeatList, OrderStatus.CANCEL);
            throw new TicketFlowFrameException(createOrderResponse);
        }
        return createOrderResponse.getData();
    }

    private String createOrderByMq(OrderCreateDto orderCreateDto, List<SeatVo> purchaseSeatList) {
        CreateOrderMqDomain createOrderMqDomain = new CreateOrderMqDomain();
        CountDownLatch latch = new CountDownLatch(1);
        createOrderSend.sendMessage(JSON.toJSONString(orderCreateDto), sendResult -> {
            createOrderMqDomain.orderNumber = String.valueOf(orderCreateDto.getOrderNumber());
            assert sendResult != null;
            log.info("创建订单kafka发送消息成功 topic : {}", sendResult.getRecordMetadata().topic());
            latch.countDown();
        }, ex -> {
            log.error("创建订单kafka发送消息失败 error", ex);
            log.error("创建订单失败 需人工处理 orderCreateDto : {}", JSON.toJSONString(orderCreateDto));
            updateProgramCacheDataResolution(orderCreateDto.getProgramId(), purchaseSeatList, OrderStatus.CANCEL);
            createOrderMqDomain.ticketFlowFrameException = new TicketFlowFrameException(ex);
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("createOrderByMq InterruptedException", e);
            throw new TicketFlowFrameException(e);
        }
        if (Objects.nonNull(createOrderMqDomain.ticketFlowFrameException)) {
            throw createOrderMqDomain.ticketFlowFrameException;
        }
        return createOrderMqDomain.orderNumber;
    }

    private void updateProgramCacheDataResolution(Long programId, List<SeatVo> seatVoList, OrderStatus orderStatus) {
        // 只能是未支付和已取消两种状态，以进行库存更改
        if (!(Objects.equals(orderStatus.getCode(), OrderStatus.NO_PAY.getCode()) || Objects.equals(orderStatus.getCode(), OrderStatus.CANCEL.getCode()))) {
            throw new TicketFlowFrameException(BaseCode.OPERATE_ORDER_STATUS_NOT_PERMIT);
        }
        List<String> keys = new ArrayList<>();
        keys.add("#");
        // 进行lua脚本数据构建
        String[] data = new String[3];
        Map<Long, Long> ticketCategoryCountMap = seatVoList.stream().collect(Collectors.groupingBy(SeatVo::getTicketCategoryId, Collectors.counting()));
        JSONArray jsonArray = new JSONArray();
        ticketCategoryCountMap.forEach((k, v) -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("programTicketRemainNumberHashKey", RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_TICKET_REMAIN_NUMBER_HASH_RESOLUTION, programId, k).getRelKey());
            jsonObject.put("ticketCategoryId", String.valueOf(k));
            // 根据订单状态判断是添加还是扣减
            if (Objects.equals(orderStatus.getCode(), OrderStatus.NO_PAY.getCode())) {
                jsonObject.put("count", "-" + v);
            } else if (Objects.equals(orderStatus.getCode(), OrderStatus.CANCEL.getCode())) {
                jsonObject.put("count", v);
            }
            jsonArray.add(jsonObject);
        });
        Map<Long, List<SeatVo>> seatVoMap = seatVoList.stream().collect(Collectors.groupingBy(SeatVo::getTicketCategoryId));
        JSONArray delSeatIdjsonArray = new JSONArray();
        JSONArray addSeatDatajsonArray = new JSONArray();
        seatVoMap.forEach((k, v) -> {
            JSONObject delSeatIdjsonObject = new JSONObject();
            JSONObject seatDatajsonObject = new JSONObject();
            String seatHashKeyDel = "";
            String seatHashKeyAdd = "";
            if (Objects.equals(orderStatus.getCode(), OrderStatus.NO_PAY.getCode())) {
                seatHashKeyDel = (RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_NO_SOLD_RESOLUTION_HASH, programId, k).getRelKey());
                seatHashKeyAdd = (RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_LOCK_RESOLUTION_HASH, programId, k).getRelKey());
                for (SeatVo seatVo : v) {
                    seatVo.setSellStatus(SellStatus.LOCK.getCode());
                }
            } else if (Objects.equals(orderStatus.getCode(), OrderStatus.CANCEL.getCode())) {
                seatHashKeyDel = (RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_LOCK_RESOLUTION_HASH, programId, k).getRelKey());
                seatHashKeyAdd = (RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_NO_SOLD_RESOLUTION_HASH, programId, k).getRelKey());
                for (SeatVo seatVo : v) {
                    seatVo.setSellStatus(SellStatus.NO_SOLD.getCode());
                }
            }
            delSeatIdjsonObject.put("seatHashKeyDel", seatHashKeyDel);
            delSeatIdjsonObject.put("seatIdList", v.stream().map(SeatVo::getId).map(String::valueOf).collect(Collectors.toList()));
            delSeatIdjsonArray.add(delSeatIdjsonObject);
            seatDatajsonObject.put("seatHashKeyAdd", seatHashKeyAdd);
            List<String> seatDataList = new ArrayList<>();
            for (SeatVo seatVo : v) {
                seatDataList.add(String.valueOf(seatVo.getId()));
                seatDataList.add(JSON.toJSONString(seatVo));
            }
            seatDatajsonObject.put("seatDataList", seatDataList);
            addSeatDatajsonArray.add(seatDatajsonObject);
        });

        data[0] = JSON.toJSONString(jsonArray);
        data[1] = JSON.toJSONString(delSeatIdjsonArray);
        data[2] = JSON.toJSONString(addSeatDatajsonArray);
        programCacheResolutionOperate.programCacheOperate(keys, data);
    }
}
