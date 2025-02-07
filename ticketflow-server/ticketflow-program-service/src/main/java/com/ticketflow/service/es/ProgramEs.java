package com.ticketflow.service.es;

import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageInfo;
import com.ticketflow.core.SpringUtil;
import com.ticketflow.dto.*;
import com.ticketflow.enums.BusinessStatus;
import com.ticketflow.page.PageUtil;
import com.ticketflow.page.PageVo;
import com.ticketflow.service.init.ProgramDocumentParamName;
import com.ticketflow.service.tool.ProgramPageOrder;
import com.ticketflow.util.BusinessEsHandle;
import com.ticketflow.utils.StringUtil;
import com.ticketflow.vo.ProgramHomeVo;
import com.ticketflow.vo.ProgramListVo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @Description: 节目ES服务
 * @Author: rickey-c
 * @Date: 2025/2/7 22:06
 */
@Slf4j
@Component
public class ProgramEs {

    @Autowired
    private BusinessEsHandle businessEsHandle;

    public List<ProgramHomeVo> selectHomeList(ProgramListDto programListDto) {
        List<ProgramHomeVo> programHomeVoList = new ArrayList<>();

        try {
            //按照父节目id集合来进行循环从es中查询，主页页面是4个父节目，也就是循环4次
            for (Long parentProgramCategoryId : programListDto.getParentProgramCategoryIds()) {
                List<EsDataQueryDto> programEsQueryDto = new ArrayList<>();
                if (Objects.nonNull(programListDto.getAreaId())) {
                    //地区id
                    EsDataQueryDto areaIdQueryDto = new EsDataQueryDto();
                    areaIdQueryDto.setParamName(ProgramDocumentParamName.AREA_ID);
                    areaIdQueryDto.setParamValue(programListDto.getAreaId());
                    programEsQueryDto.add(areaIdQueryDto);
                } else {
                    EsDataQueryDto primeQueryDto = new EsDataQueryDto();
                    primeQueryDto.setParamName(ProgramDocumentParamName.PRIME);
                    primeQueryDto.setParamValue(BusinessStatus.YES.getCode());
                    programEsQueryDto.add(primeQueryDto);
                }

                //父节目类型id集合
                EsDataQueryDto parentProgramCategoryIdQueryDto = new EsDataQueryDto();
                parentProgramCategoryIdQueryDto.setParamName(ProgramDocumentParamName.PARENT_PROGRAM_CATEGORY_ID);
                parentProgramCategoryIdQueryDto.setParamValue(parentProgramCategoryId);
                programEsQueryDto.add(parentProgramCategoryIdQueryDto);
                //查询前7条
                PageInfo<ProgramListVo> pageInfo = businessEsHandle.queryPage(
                        SpringUtil.getPrefixDistinctionName() + "-" + ProgramDocumentParamName.INDEX_NAME,
                        ProgramDocumentParamName.INDEX_TYPE, programEsQueryDto, 1, 7, ProgramListVo.class);
                if (!pageInfo.getList().isEmpty()) {
                    ProgramHomeVo programHomeVo = new ProgramHomeVo();
                    programHomeVo.setCategoryName(pageInfo.getList().get(0).getParentProgramCategoryName());
                    programHomeVo.setCategoryId(pageInfo.getList().get(0).getParentProgramCategoryId());
                    programHomeVo.setProgramListVoList(pageInfo.getList());
                    programHomeVoList.add(programHomeVo);
                }
            }
        } catch (Exception e) {
            log.error("businessEsHandle.queryPage error", e);
        }
        return programHomeVoList;
    }

    public List<ProgramListVo> recommendList(ProgramRecommendListDto programRecommendListDto) {
        List<ProgramListVo> programListVoList = new ArrayList<>();
        try {
            boolean allQueryFlag = true;
            MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            if (Objects.nonNull(programRecommendListDto.getAreaId())) {
                allQueryFlag = false;
                QueryBuilder builds = QueryBuilders.termQuery(ProgramDocumentParamName.AREA_ID,
                        programRecommendListDto.getAreaId());
                boolQuery.must(builds);
            }
            if (Objects.nonNull(programRecommendListDto.getParentProgramCategoryId())) {
                allQueryFlag = false;
                QueryBuilder builds = QueryBuilders.termQuery(ProgramDocumentParamName.PARENT_PROGRAM_CATEGORY_ID,
                        programRecommendListDto.getParentProgramCategoryId());
                boolQuery.must(builds);
            }
            if (Objects.nonNull(programRecommendListDto.getProgramId())) {
                allQueryFlag = false;
                QueryBuilder builds = QueryBuilders.termQuery(ProgramDocumentParamName.ID,
                        programRecommendListDto.getProgramId());
                boolQuery.mustNot(builds);
            }
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(allQueryFlag ? matchAllQueryBuilder : boolQuery);
            searchSourceBuilder.trackTotalHits(true);
            searchSourceBuilder.from(1);
            searchSourceBuilder.size(10);

            Script script = new Script("Math.random()");
            ScriptSortBuilder scriptSortBuilder = new ScriptSortBuilder(script, ScriptSortBuilder.ScriptSortType.NUMBER);
            scriptSortBuilder.order(SortOrder.ASC);

            searchSourceBuilder.sort(scriptSortBuilder);

            businessEsHandle.executeQuery(
                    SpringUtil.getPrefixDistinctionName() + "-" + ProgramDocumentParamName.INDEX_NAME,
                    ProgramDocumentParamName.INDEX_TYPE, programListVoList, null, ProgramListVo.class,
                    searchSourceBuilder, null);
        } catch (Exception e) {
            log.error("recommendList error", e);
        }
        return programListVoList;
    }


    public PageVo<ProgramListVo> selectPage(ProgramPageListDto programPageListDto) {
        PageVo<ProgramListVo> pageVo = new PageVo<>();
        try {
            List<EsDataQueryDto> esDataQueryDtoList = new ArrayList<>();
            if (Objects.nonNull(programPageListDto.getAreaId())) {
                // 地区非空，就填入地区参数进行查询
                EsDataQueryDto areaIdQueryDto = new EsDataQueryDto();
                areaIdQueryDto.setParamName(ProgramDocumentParamName.AREA_ID);
                areaIdQueryDto.setParamValue(programPageListDto.getAreaId());
                esDataQueryDtoList.add(areaIdQueryDto);
            } else {
                // 地区为空，查询节目分组内的主要节目
                EsDataQueryDto primeQueryDto = new EsDataQueryDto();
                primeQueryDto.setParamName(ProgramDocumentParamName.PRIME);
                primeQueryDto.setParamValue(BusinessStatus.YES.getCode());
                esDataQueryDtoList.add(primeQueryDto);
            }
            // 父节目类型条件
            if (Objects.nonNull(programPageListDto.getParentProgramCategoryId())) {
                EsDataQueryDto parentProgramCategoryIdQueryDto = new EsDataQueryDto();
                parentProgramCategoryIdQueryDto.setParamName(ProgramDocumentParamName.PARENT_PROGRAM_CATEGORY_ID);
                parentProgramCategoryIdQueryDto.setParamValue(programPageListDto.getParentProgramCategoryId());
                esDataQueryDtoList.add(parentProgramCategoryIdQueryDto);
            }
            // 节目类型条件
            if (Objects.nonNull(programPageListDto.getProgramCategoryId())) {
                EsDataQueryDto programCategoryIdQueryDto = new EsDataQueryDto();
                programCategoryIdQueryDto.setParamName(ProgramDocumentParamName.PROGRAM_CATEGORY_ID);
                programCategoryIdQueryDto.setParamValue(programPageListDto.getProgramCategoryId());
                esDataQueryDtoList.add(programCategoryIdQueryDto);
            }
            // 时间条件
            if (Objects.nonNull(programPageListDto.getStartDateTime()) &&
                    Objects.nonNull(programPageListDto.getEndDateTime())) {
                EsDataQueryDto showDayTimeQueryDto = new EsDataQueryDto();
                showDayTimeQueryDto.setParamName(ProgramDocumentParamName.SHOW_DAY_TIME);
                showDayTimeQueryDto.setStartTime(programPageListDto.getStartDateTime());
                showDayTimeQueryDto.setEndTime(programPageListDto.getEndDateTime());
                esDataQueryDtoList.add(showDayTimeQueryDto);
            }
            // 获取排序字段
            ProgramPageOrder programPageOrder = getProgramPageOrder(programPageListDto);

            PageInfo<ProgramListVo> programListVoPageInfo = businessEsHandle.queryPage(
                    SpringUtil.getPrefixDistinctionName() + "-" + ProgramDocumentParamName.INDEX_NAME,
                    ProgramDocumentParamName.INDEX_TYPE, esDataQueryDtoList, programPageOrder.sortParam,
                    programPageOrder.sortOrder, programPageListDto.getPageNumber(), programPageListDto.getPageSize(),
                    ProgramListVo.class);
            // pageVo是为了整合MybatisPlus和ES，不能让这两个相互依赖，因此自定义了一个PageVo类
            pageVo = PageUtil.convertPage(programListVoPageInfo, programListVo -> programListVo);
        } catch (Exception e) {
            log.error("selectPage error", e);
        }
        return pageVo;
    }

    public ProgramPageOrder getProgramPageOrder(ProgramPageListDto programPageListDto) {
        ProgramPageOrder programPageOrder = new ProgramPageOrder();
        switch (programPageListDto.getType()) {
            //推荐排序
            case 2:
                programPageOrder.sortParam = ProgramDocumentParamName.HIGH_HEAT;
                programPageOrder.sortOrder = SortOrder.DESC;
                break;
            //最近开场    
            case 3:
                programPageOrder.sortParam = ProgramDocumentParamName.SHOW_TIME;
                programPageOrder.sortOrder = SortOrder.ASC;
                break;
            //最新上架    
            case 4:
                programPageOrder.sortParam = ProgramDocumentParamName.ISSUE_TIME;
                programPageOrder.sortOrder = SortOrder.ASC;
                break;
            //相关度排序    
            default:
                programPageOrder.sortParam = null;
                programPageOrder.sortOrder = null;
        }
        return programPageOrder;
    }

    public PageVo<ProgramListVo> search(ProgramSearchDto programSearchDto) {
        PageVo<ProgramListVo> pageVo = new PageVo<>();
        try {
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            if (Objects.nonNull(programSearchDto.getAreaId())) {
                QueryBuilder builds = QueryBuilders.termQuery(ProgramDocumentParamName.AREA_ID, programSearchDto.getAreaId());
                boolQuery.must(builds);
            }
            if (Objects.nonNull(programSearchDto.getParentProgramCategoryId())) {
                QueryBuilder builds = QueryBuilders.termQuery(ProgramDocumentParamName.PARENT_PROGRAM_CATEGORY_ID, programSearchDto.getParentProgramCategoryId());
                boolQuery.must(builds);
            }
            if (Objects.nonNull(programSearchDto.getStartDateTime()) &&
                    Objects.nonNull(programSearchDto.getEndDateTime())) {
                QueryBuilder builds = QueryBuilders.rangeQuery(ProgramDocumentParamName.SHOW_DAY_TIME)
                        .from(programSearchDto.getStartDateTime()).to(programSearchDto.getEndDateTime()).includeLower(true);
                boolQuery.must(builds);
            }
            if (StringUtil.isNotEmpty(programSearchDto.getContent())) {
                BoolQueryBuilder innerBoolQuery = QueryBuilders.boolQuery();
                innerBoolQuery.should(QueryBuilders.matchQuery(ProgramDocumentParamName.TITLE, programSearchDto.getContent()));
                innerBoolQuery.should(QueryBuilders.matchQuery(ProgramDocumentParamName.ACTOR, programSearchDto.getContent()));
                innerBoolQuery.minimumShouldMatch(1);
                boolQuery.must(innerBoolQuery);
            }

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            ProgramPageOrder programPageOrder = getProgramPageOrder(programSearchDto);
            if (Objects.nonNull(programPageOrder.sortParam) && Objects.nonNull(programPageOrder.sortOrder)) {
                FieldSortBuilder sort = SortBuilders.fieldSort(programPageOrder.sortParam);
                sort.order(programPageOrder.sortOrder);
                searchSourceBuilder.sort(sort);
            }
            searchSourceBuilder.query(boolQuery);
            searchSourceBuilder.trackTotalHits(true);
            searchSourceBuilder.from((programSearchDto.getPageNumber() - 1) * programSearchDto.getPageSize());
            searchSourceBuilder.size(programSearchDto.getPageSize());
            searchSourceBuilder.highlighter(getHighlightBuilder(Arrays.asList(ProgramDocumentParamName.TITLE,
                    ProgramDocumentParamName.ACTOR)));
            List<ProgramListVo> list = new ArrayList<>();
            PageInfo<ProgramListVo> pageInfo = new PageInfo<>(list);
            pageInfo.setPageNum(programSearchDto.getPageNumber());
            pageInfo.setPageSize(programSearchDto.getPageSize());
            businessEsHandle.executeQuery(SpringUtil.getPrefixDistinctionName() + "-" + ProgramDocumentParamName.INDEX_NAME,
                    ProgramDocumentParamName.INDEX_TYPE, list, pageInfo, ProgramListVo.class,
                    searchSourceBuilder, Arrays.asList(ProgramDocumentParamName.TITLE, ProgramDocumentParamName.ACTOR));
            pageVo = PageUtil.convertPage(pageInfo, programListVo -> programListVo);
        } catch (Exception e) {
            log.error("search error", e);
        }
        return pageVo;
    }

    public HighlightBuilder getHighlightBuilder(List<String> fieldNameList) {
        // 创建一个HighlightBuilder
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        for (String fieldName : fieldNameList) {
            // 为特定字段添加高亮设置
            HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field(fieldName);
            highlightTitle.preTags("<em>");
            highlightTitle.postTags("</em>");
            highlightBuilder.field(highlightTitle);
        }
        return highlightBuilder;
    }

    public void deleteByProgramId(Long programId) {
        try {
            List<EsDataQueryDto> esDataQueryDtoList = new ArrayList<>();
            EsDataQueryDto programIdDto = new EsDataQueryDto();
            programIdDto.setParamName(ProgramDocumentParamName.ID);
            programIdDto.setParamValue(programId);
            esDataQueryDtoList.add(programIdDto);

            List<ProgramListVo> programListVos =
                    businessEsHandle.query(
                            SpringUtil.getPrefixDistinctionName() + "-" + ProgramDocumentParamName.INDEX_NAME,
                            ProgramDocumentParamName.INDEX_TYPE,
                            esDataQueryDtoList,
                            ProgramListVo.class);
            if (CollectionUtil.isNotEmpty(programListVos)) {
                for (ProgramListVo programListVo : programListVos) {
                    businessEsHandle.deleteByDocumentId(
                            SpringUtil.getPrefixDistinctionName() + "-" + ProgramDocumentParamName.INDEX_NAME,
                            programListVo.getEsId());
                }
            }
        } catch (Exception e) {
            log.error("deleteByProgramId error", e);
        }
    }
}
