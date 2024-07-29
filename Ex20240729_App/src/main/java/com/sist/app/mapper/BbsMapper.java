package com.sist.app.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.sist.app.vo.BbsVO;

@Mapper
public interface BbsMapper {
    
    int count(String bname, String searchType, String searchValue);

    List<BbsVO> bbsList(String bname, String searchType, String searchValue, int begin, int end);

}
